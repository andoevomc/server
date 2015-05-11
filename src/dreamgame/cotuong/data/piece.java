package dreamgame.cotuong.data;

/**
 *
 * @author Dinhpv
 */
public class piece {

    public int col, row;
    public int name;
    public boolean isBlk;
    public boolean focusOn;
    public boolean visible = true;

    public piece() {
    }

    public piece(int n, int c, int r, boolean isB, boolean f) {
        col = c;
        row = r;
        name = n;
        isBlk = isB;
        focusOn = f;
    }

    public void setVisible(boolean vis) {
        visible = vis;
    }

    public int getFrameNo() {
        if (isBlk) {
            return name + 7;
        } else {
            return name;
        }
    }

    public void copyfrom(piece pc) {
        this.col = pc.col;
        this.row = pc.row;
        this.name = pc.name;
        this.isBlk = pc.isBlk;
        this.focusOn = pc.focusOn;
        this.visible = pc.visible;
    }
}
