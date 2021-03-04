package motion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

/**
 * 
 * Esta classe usa-se de forma estática
 *
 */
public class AnimatedSearch extends Component {
	private static final long serialVersionUID = 1L;
	
	//Nota: tenham em conta os efeitos colaterais na atribuição deste campo.
	static BufferedImage img = null;
	
	static Component c = new AnimatedSearch();
	
	public AnimatedSearch() {
    }
	
    public void paint(Graphics g) {
    	g.setPaintMode();
        g.drawImage(img, 0, 0, null);
    }
    
    /**
     * Este método tem de ser chamado antes do draw().
     */
    public static void init(String image) {
    	JFrame f2 = new JFrame("Explored");
        BitmapTerrain t = new BitmapTerrain(image);
        img = t.getTerrain();
        
        f2.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        f2.add(c);
        f2.pack();
        f2.setVisible(true);
        
    }
    
    /**
     * Método que pinta um pixel e refresca a janela. A cor fica a gosto
     * A minha sugestao é que sempre que um estado È expandido, pintamos o pixel
     * correspondente ao mesmo.
     * @param x
     * @param y
     */
    public static void draw(int x, int y) {
    	if( img != null ) {
    		img.setRGB(x, y, Color.MAGENTA.getRGB());
    		c.repaint();
    	}
    }
    
    
    public Dimension getPreferredSize() {
        if (img == null) {
             return new Dimension(100,100);
        } else {
           return new Dimension(img.getWidth(null), img.getHeight(null));
       }
    }
    
    public static BufferedImage getImage() {
    	return img;
    }
    
}
