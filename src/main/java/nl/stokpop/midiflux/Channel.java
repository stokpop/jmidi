package nl.stokpop.midiflux;

public enum Channel {

    c1(0),c2(1),c3(2),c4(3),c5(4),c6(5),c7(6),c8(7),c9(8),c10(9),c11(10),c12(11),c13(12),c14(13),c15(14),c16(15);

    private int nr;

    Channel(int nr) {
        this.nr = nr;
    }

    public int getNr() {
        return nr;
    }
}
