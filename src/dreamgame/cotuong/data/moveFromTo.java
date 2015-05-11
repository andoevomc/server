package dreamgame.cotuong.data;

/**
 *
 * @author Dinhpv
 */
public final class moveFromTo
{
    public int fromCol;
    public int fromRow;
    public int toCol;
    public int toRow;
    
    public moveFromTo(){}
    
    public moveFromTo(int fromC, int fromR, int toC, int toR)
    {
        fromCol=fromC;
        fromRow=fromR;
        toCol=toC;
        toRow=toR;
    }

    public void copyfrom(moveFromTo mv)
    {
        fromCol=mv.fromCol;
        fromRow=mv.fromRow;
        toCol=mv.toCol;
        toRow=mv.toRow;
    }
}