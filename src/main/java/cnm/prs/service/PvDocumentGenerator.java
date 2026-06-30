package cnm.prs.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.springframework.stereotype.Component;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import cnm.prs.exception.BusinessRuleException;
import jakarta.annotation.PreDestroy;

/**
 * Génère le PDF du <strong>Projet de PV pour avis favorable sous réserve</strong> à partir du modèle Word
 * {@code resources/templates/PV_AFSR_PPMAGPM_CENTRALE.docx} : copie du {@code .docx}, remplacement des
 * placeholders (au niveau du paragraphe, gère les runs scindés), puis conversion en PDF via Microsoft Word
 * (documents4j local), comme la lettre de renvoi. La mise en forme et l'emblème du modèle sont conservés.
 *
 * <p>Particularités du modèle PV :</p>
 * <ul>
 *   <li>{@code <DATE EXAMEN>} apparaît 2 fois : formatée « jj mois aaaa » dans « Séance du », et
 *       <strong>en toutes lettres</strong> dans le paragraphe « L'an … » (détecté par « instituée »).</li>
 *   <li>Bloc « Étaient présents » : les lignes Président / Chef de commission ne sont conservées que si
 *       le nom est fourni (rôle ayant signé) ; Membre et Secrétaire de séance sont toujours présents.</li>
 *   <li>ANNEXE : la ligne modèle du tableau est dupliquée pour chaque observation (point / au lieu de / lire).</li>
 * </ul>
 */
@Component
public class PvDocumentGenerator {

    private static final String MODELE = "/templates/PV_AFSR_PPMAGPM_CENTRALE.docx";

    private static final String DATE_EXAMEN = "<DATE EXAMEN>";
    private static final String REFERENCE_PV = "<REFERENCE PV >";
    private static final String DATE_RECEPTION = "<DATE RECEPTION DU DOSSIER>";
    private static final String ENTITE = "<ENTITE CONTRACTANTE>";
    private static final String ANNEE = "<ANNEE EXERCICE>";
    private static final String PRESIDENT = "<NOM ET PRENOMS DU PRESIDENT>";
    private static final String CHEF_COMMISSION = "<NOM ET PRENOMS DU CHEF DE COMMISSION>";
    private static final String MEMBRE = "<NOM ET PRENOMS DU MEMBRE>";
    private static final String VERIFICATEUR = "<NOM ET PRENOMS DU VERIFICATEUR>";
    private static final String LOCALITE = "<LOCALITE>";
    private static final String DATE_AUJOURDHUI = "<DATE AUJOURD’HUI>";
    private static final String POINT = "<POINT DE CONTROLE>";
    private static final String AU_LIEU_DE = "<AU LIEU DE>";
    private static final String LIRE = "<LIRE>";

    /** Marqueur du paragraphe « L'an … » (date en toutes lettres). */
    private static final String MARQUEUR_LAN = "instituée";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);

    /** Convertisseur Word partagé, initialisé paresseusement (cf. lettre de renvoi). */
    private volatile IConverter convertisseur;

    /** @return le PDF du Projet de PV (copie du modèle, placeholders remplis). */
    public byte[] genererPdf(PvDocumentContexte ctx) {
        try (InputStream in = getClass().getResourceAsStream(MODELE)) {
            if (in == null) {
                throw new BusinessRuleException("Modèle de PV introuvable : " + MODELE);
            }
            XWPFDocument doc = new XWPFDocument(in);
            remplirCorps(doc, ctx);
            remplirTablesHorsAnnexe(doc, baseMap(ctx));
            remplirAnnexe(doc, ctx);
            ByteArrayOutputStream docxOut = new ByteArrayOutputStream();
            doc.write(docxOut);
            doc.close();

            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            convertisseur().convert(new ByteArrayInputStream(docxOut.toByteArray()))
                    .as(DocumentType.DOCX).to(pdfOut).as(DocumentType.PDF).execute();
            return pdfOut.toByteArray();
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessRuleException("Génération du document du PV impossible : " + e.getMessage());
        }
    }

    /** Placeholders constants (hors {@code <DATE EXAMEN>} et lignes conditionnelles président/CC). */
    private Map<String, String> baseMap(PvDocumentContexte ctx) {
        Map<String, String> m = new HashMap<>();
        m.put(REFERENCE_PV, nz(ctx.refPv()));
        m.put(DATE_RECEPTION, fmt(ctx.dateReception()));
        m.put(ENTITE, nz(ctx.entiteContractante()));
        m.put(ANNEE, ctx.anneeExercice() == null ? "" : String.valueOf(ctx.anneeExercice()));
        m.put(MEMBRE, nz(ctx.nomMembre()));
        m.put(VERIFICATEUR, nz(ctx.nomVerificateur()));
        m.put(LOCALITE, nz(ctx.localite()));
        m.put(DATE_AUJOURDHUI, fmt(LocalDate.now()));
        return m;
    }

    /** Corps (paragraphes hors tableaux) : double date, lignes présents conditionnelles, autres placeholders. */
    private void remplirCorps(XWPFDocument doc, PvDocumentContexte ctx) {
        Map<String, String> base = baseMap(ctx);
        boolean president = nonVide(ctx.nomPresident());
        boolean chef = nonVide(ctx.nomChefCommission());
        List<XWPFParagraph> aSupprimer = new ArrayList<>();
        for (XWPFParagraph p : doc.getParagraphs()) {
            String texte = texteConcatene(p);
            if (texte.isEmpty()) {
                continue;
            }
            if (texte.contains(PRESIDENT) && !president) {
                aSupprimer.add(p);
                continue;
            }
            if (texte.contains(CHEF_COMMISSION) && !chef) {
                aSupprimer.add(p);
                continue;
            }
            Map<String, String> m = new HashMap<>(base);
            if (president) {
                m.put(PRESIDENT, ctx.nomPresident());
            }
            if (chef) {
                m.put(CHEF_COMMISSION, ctx.nomChefCommission());
            }
            if (texte.contains(DATE_EXAMEN)) {
                m.put(DATE_EXAMEN, texte.contains(MARQUEUR_LAN)
                        ? NombreEnLettres.dateEnLettres(ctx.dateExamen()) : fmt(ctx.dateExamen()));
            }
            remplacerDansParagraphe(p, m);
        }
        for (XWPFParagraph p : aSupprimer) {
            int pos = doc.getBodyElements().indexOf(p);
            if (pos >= 0) {
                doc.removeBodyElement(pos);
            }
        }
    }

    /** Tableaux hors ANNEXE (bloc de signature : {@code <LOCALITE>}, {@code <DATE AUJOURD'HUI>}). */
    private void remplirTablesHorsAnnexe(XWPFDocument doc, Map<String, String> base) {
        for (XWPFTable table : doc.getTables()) {
            if (table.getText() != null && table.getText().contains(POINT)) {
                continue;   // ANNEXE traitée à part
            }
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    cell.getParagraphs().forEach(p -> remplacerDansParagraphe(p, base));
                }
            }
        }
    }

    /** ANNEXE : duplique la ligne modèle pour chaque observation, puis retire la ligne modèle. */
    private void remplirAnnexe(XWPFDocument doc, PvDocumentContexte ctx) {
        XWPFTable annexe = null;
        for (XWPFTable t : doc.getTables()) {
            if (t.getText() != null && t.getText().contains(POINT)) {
                annexe = t;
                break;
            }
        }
        if (annexe == null) {
            return;
        }
        int idxModele = -1;
        for (int i = 0; i < annexe.getRows().size(); i++) {
            if (annexe.getRow(i).getCtRow().toString().contains(POINT) || ligneContient(annexe.getRow(i), POINT)) {
                idxModele = i;
                break;
            }
        }
        if (idxModele < 0) {
            return;
        }
        XWPFTableRow modele = annexe.getRow(idxModele);
        List<PvDocumentContexte.Observation> obs = ctx.observations() == null ? List.of() : ctx.observations();
        int insert = idxModele;
        for (PvDocumentContexte.Observation o : obs) {
            CTRow ct = (CTRow) modele.getCtRow().copy();
            XWPFTableRow ligne = new XWPFTableRow(ct, annexe);
            Map<String, String> m = new HashMap<>();
            m.put(POINT, nz(o.pointControle()));
            m.put(AU_LIEU_DE, nz(o.auLieuDe()));
            m.put(LIRE, nz(o.lire()));
            for (XWPFTableCell cell : ligne.getTableCells()) {
                cell.getParagraphs().forEach(p -> remplacerDansParagraphe(p, m));
            }
            annexe.addRow(ligne, insert++);
        }
        annexe.removeRow(insert);   // retire la ligne modèle (décalée en `insert` après les insertions)
    }

    private boolean ligneContient(XWPFTableRow row, String token) {
        for (XWPFTableCell cell : row.getTableCells()) {
            for (XWPFParagraph p : cell.getParagraphs()) {
                if (texteConcatene(p).contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Remplace les placeholders d'un paragraphe en raisonnant sur le texte concaténé des runs. */
    private void remplacerDansParagraphe(XWPFParagraph paragraphe, Map<String, String> rempl) {
        List<XWPFRun> runs = paragraphe.getRuns();
        if (runs == null || runs.isEmpty()) {
            return;
        }
        String texte = texteConcatene(paragraphe);
        if (texte.isEmpty() || !contientUnPlaceholder(texte, rempl)) {
            return;
        }
        String remplace = texte;
        for (Map.Entry<String, String> e : rempl.entrySet()) {
            remplace = remplace.replace(e.getKey(), e.getValue() == null ? "" : e.getValue());
        }
        runs.get(0).setText(remplace, 0);
        for (int i = runs.size() - 1; i >= 1; i--) {
            paragraphe.removeRun(i);
        }
    }

    private String texteConcatene(XWPFParagraph paragraphe) {
        List<XWPFRun> runs = paragraphe.getRuns();
        if (runs == null || runs.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (XWPFRun run : runs) {
            String t = run.getText(0);
            if (t != null) {
                sb.append(t);
            }
        }
        return sb.toString();
    }

    private boolean contientUnPlaceholder(String texte, Map<String, String> rempl) {
        for (String cle : rempl.keySet()) {
            if (texte.contains(cle)) {
                return true;
            }
        }
        return false;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static boolean nonVide(String s) {
        return s != null && !s.isBlank();
    }

    private static String fmt(LocalDate d) {
        return d == null ? "" : d.format(FMT);
    }

    private IConverter convertisseur() {
        IConverter c = convertisseur;
        if (c == null) {
            synchronized (this) {
                c = convertisseur;
                if (c == null) {
                    c = LocalConverter.builder().build();
                    convertisseur = c;
                }
            }
        }
        return c;
    }

    @PreDestroy
    void fermerConvertisseur() {
        IConverter c = convertisseur;
        if (c != null) {
            c.shutDown();
        }
    }
}
