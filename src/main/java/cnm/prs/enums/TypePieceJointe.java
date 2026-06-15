package cnm.prs.enums;

/**
 * Types de pièces jointes à une inscription PRMP (colonne {@code t_piece_jointe.TYPE_PIECE}).
 * Chaque type porte sa taille maximale autorisée.
 */
public enum TypePieceJointe {

    /** Arrêté de nomination (obligatoire). */
    ARRETE_NOMIN(10L * 1024 * 1024),

    /** Carte d'identité nationale (obligatoire). */
    CIN(5L * 1024 * 1024),

    /** Photo (optionnelle). */
    PHOTO(5L * 1024 * 1024);

    private final long maxOctets;

    TypePieceJointe(long maxOctets) {
        this.maxOctets = maxOctets;
    }

    /** Taille maximale autorisée pour ce type, en octets. */
    public long maxOctets() {
        return maxOctets;
    }
}
