import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.UIManager;

/**
 * A class that represents a picture made up of a rectangle of {@link Pixel}s
 */
public class Picture {
    
    /** The 2D array of pixels that comprise this picture */
    private Pixel[][] pixels;

    /**
     * Creates a Picture from an image file in the "images" directory
     * @param picture The name of the file to load
     */
    public Picture(String picture) {
        File file = new File("./images/"+picture);
        BufferedImage image;
        if (!file.exists()) throw new RuntimeException("No picture at the location "+file.getPath()+"!");
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        pixels = new Pixel[image.getHeight()][image.getWidth()];
        for (int y = 0; y<pixels.length; y++) {
            for (int x = 0; x<pixels[y].length; x++) {
                int rgb = image.getRGB(x, y);
                /*
                 * For the curious - BufferedImage saves an image's RGB info into a hexadecimal integer
                 * The below extracts the individual values using bit-shifting and bit-wise ANDing with all 1's
                 */
                pixels[y][x] = new Pixel((rgb>>16)&0xff, (rgb>>8)&0xff, rgb&0xff);
            }
        }
    }

    /**
     * Creates a solid-color Picture of a given color, width, and height
     * @param red The red value of the color
     * @param green The green value of the color
     * @param blue The blue value of the color
     * @param height The height of the Picture
     * @param width The width of the Picture
     */
    public Picture(int red, int green, int blue, int height, int width) {
        pixels = new Pixel[height][width];
        for (int y = 0; y<pixels.length; y++) {
            for (int x = 0; x<pixels[y].length; x++) {
                pixels[y][x] = new Pixel(red, green, blue);
            }
        }
    }

    /**
     * Creates a solid white Picture of a given width and height
     * @param color The {@link Color} of the Picture
     * @param height The height of the Picture
     * @param width The width of the Picture
     */
    public Picture(int height, int width) {
        this(Color.WHITE, height, width);
    }

    /**
     * Creates a solid-color Picture of a given color, width, and height
     * @param color The {@link Color} of the Picture
     * @param width The width of the Picture
     * @param height The height of the Picture
     */
    public Picture(Color color, int height, int width) {
        this(color.getRed(), color.getGreen(), color.getBlue(), height, width);
    }

    /**
     * Creates a Picture based off of an existing {@link Pixel} 2D array
     * @param pixels A rectangular 2D array of {@link Pixel}s. Must be at least 1x1
     */
    public Picture(Pixel[][] pixels) {
        if (pixels.length==0 || pixels[0].length==0) throw new RuntimeException("Can't have an empty image!");
        int width = pixels[0].length;
        for (int i = 0; i<pixels.length; i++) if (pixels[i].length!=width)
            throw new RuntimeException("Pictures must be rectangles. pixels[0].length!=pixels["+i+"].length!");
        this.pixels = new Pixel[pixels.length][width];
        for (int i = 0; i<pixels.length; i++) {
            for (int j = 0; j<pixels[i].length; j++) {
                this.pixels[i][j] = new Pixel(pixels[i][j].getColor());
            }
        }
    }

    /**
     * Creates a Picture based off of an existing Picture
     * @param picture The Picture to copy
     */
    public Picture(Picture picture) {
        this(picture.pixels);
    }

    /**
     * Gets the width of the Picture
     * @return The width of the Picture
     */
    public int getWidth() {
        return pixels[0].length;
    }

    /**
     * Gets the height of the Picture
     * @return The height of the Picture
     */
    public int getHeight() {
        return pixels.length;
    }

    /**
     * Gets the {@link Pixel} at a given coordinate
     * @param x The x location of the {@link Pixel}
     * @param y The y location of the {@link Pixel}
     * @return The {@link Pixel} at the given location
     */
    public Pixel getPixel(int x, int y) {
        if (x>=getWidth() || y>=getHeight() || x<0 || y<0) throw new RuntimeException("No pixel at ("+x+", "+y+")");
        return pixels[y][x];
    }

    /**
     * Sets the {@link Pixel} at a given coordinate
     * @param x The x location of the {@link Pixel}
     * @param y The y location of the {@link Pixel}
     * @param pixel The new {@link Pixel}
     */
    public void setPixel(int x, int y, Pixel pixel) {
        if (x>=getWidth() || y>=getHeight() || x<0 || y<0) throw new RuntimeException("No pixel at ("+x+", "+y+")");
        if (pixel==null) throw new NullPointerException("Pixel is null"); //guard is required because pixel's value isn't used in this method
        pixels[y][x] = pixel;
    }

    /**
     * Opens a {@link PictureViewer} to view this Picture
     * @return the {@link PictureViewer} viewing the Picture
     */
    public PictureViewer view() {
        return new PictureViewer(this);
    }

    /**
     * Save the image on disk as a JPEG
     * Call programmatically on a Picture object, it will prompt you to choose a save location
     * In the save dialogue window, specify the file AND extension (e.g. "lilies.jpg")
     * Extension must be .jpg as ImageIO is expecting to write a jpeg
     */
    public void save()
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } 
        catch(Exception e) {
            e.printStackTrace();
        }
        
        BufferedImage image = new BufferedImage(this.pixels[0].length, this.pixels.length, BufferedImage.TYPE_INT_RGB);

        for (int r = 0; r < this.pixels.length; r++)
            for (int c = 0; c < this.pixels[0].length; c++)
                image.setRGB(c, r, this.pixels[r][c].getColor().getRGB());

        //user's Desktop will be default directory location
        JFileChooser chooser = new JFileChooser(System.getProperty("user.home") + "/Desktop");

        chooser.setDialogTitle("Select picture save location / file name");

        File file = null;

        int choice = chooser.showSaveDialog(null);

        if (choice == JFileChooser.APPROVE_OPTION)
            file = chooser.getSelectedFile();

        //append extension if user didn't read save instructions
        if (!file.getName().endsWith(".jpg") && !file.getName().endsWith(".JPG") && !file.getName().endsWith(".jpeg") && !file.getName().endsWith(".JPEG"))
            file = new File(file.getAbsolutePath() + ".jpg");
        
        try {
            ImageIO.write(image, "jpg", file);
            System.out.println("File created at " + file.getAbsolutePath());
        }
        catch (IOException e) {
            System.out.println("Can't write to location: " + file.toString());
        }
        catch (NullPointerException|IllegalArgumentException e) {
            System.out.println("Invalid directory choice");
        }
    }
    
    /** return a copy of the reference to the 2D array of pixels that comprise this picture */
    public Pixel[][] getPixels() {
        return pixels;
    }


    /********************************************************
     *************** STUDENT METHODS BELOW ******************
     ********************************************************/

    /** remove all blue tint from a picture */
    public void zeroBlue()
    {
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                pixels[r][c].setBlue(0);
            }
        }
    }

    /** remove everything BUT blue tint from a picture */
    public void keepOnlyBlue()
    {
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                pixels[r][c].setRed(0);
                pixels[r][c].setGreen(0);
            }
        }
    }

    /** invert a picture's colors */
    public void negate()
    {
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                pixels[r][c].setColor(
                255 - pixels[r][c].getRed(), 
                255 - pixels[r][c].getGreen(), 
                255 - pixels[r][c].getGreen());
            }
        }
    }

    /** simulate the over-exposure of a picture in film processing */
    public void solarize(int threshold)
    {
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                if(pixels[r][c].getRed() < threshold)
                {
                    pixels[r][c].setRed(255 - pixels[r][c].getRed());
                }
                if(pixels[r][c].getGreen() < threshold)
                {
                    pixels[r][c].setGreen(255 - pixels[r][c].getGreen());
                }
                if(pixels[r][c].getBlue() < threshold)
                {
                    pixels[r][c].setBlue(255 - pixels[r][c].getBlue());
                }
            }
        }
    }

    /** convert an image to grayscale */
    public void grayscale()
    {
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                int avg = (pixels[r][c].getRed() + pixels[r][c].getRed() + pixels[r][c].getRed())/3;
                pixels[r][c].setColor(avg, avg, avg);
            }
        }
    }

    /** change the tint of the picture by the supplied coefficients */
    public void tint(double red, double blue, double green)
    {
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                int newRed = (int)(pixels[r][c].getRed()*red);
                int newGreen = (int)(pixels[r][c].getGreen()*green);
                int newBlue = (int)(pixels[r][c].getBlue()*blue);
                
                
                if(newRed > 255)
                {
                    newRed = 255;
                }
                if(newBlue > 255)
                {
                    newBlue = 255;
                }
                if(newGreen > 255)
                {
                    newGreen = 255;
                }
                
                pixels[r][c].setRed(newRed);
                pixels[r][c].setGreen(newGreen);
                pixels[r][c].setBlue(newBlue);
            }
        }
    }
    
    /** reduces the number of colors in an image to create a "graphic poster" effect */
    public void posterize(int span)
    {
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                pixels[r][c].setColor(
                pixels[r][c].getRed()/span * span, 
                pixels[r][c].getGreen()/span * span, 
                pixels[r][c].getBlue()/span * span);
            }
        }
    }
    
    /** mirror an image about a vertical midline, left to right */
    public void mirrorVertical()
    {
        Pixel leftPixel  = null;
        Pixel rightPixel = null;

        int width = pixels[0].length;
        
        for (int r = 0; r < pixels.length; r++)
        {
            for (int c = 0; c < width / 2; c++)
            {
                leftPixel  = pixels[r][c];
                rightPixel = pixels[r][(width - 1) - c];
                rightPixel.setColor(leftPixel.getColor());
            }
        }
    }

    
    public void mirrorRightToLeft()
    {
        Pixel leftPixel  = null;
        Pixel rightPixel = null;

        int width = pixels[0].length;
        
        for (int r = 0; r < pixels.length; r++)
        {
            for (int c = 0; c < width / 2; c++)
            {
                leftPixel  = pixels[r][c];
                rightPixel = pixels[r][(width - 1) - c];
                leftPixel.setColor(rightPixel.getColor());
            }
        }
    }

    /** mirror about a horizontal midline, top to bottom */
    public void mirrorHorizontal()
    {
        Pixel topPixel  = null;
        Pixel bottomPixel = null;

        int width = pixels[0].length;
        
        for (int c = 0; c < width; c++)
        {
            for (int r = 0; r < pixels.length/2; r++)
            {
                topPixel  = pixels[r][c];
                bottomPixel = pixels[pixels.length-r-1][c];
                bottomPixel.setColor(topPixel.getColor());
            }
        }
    }

    /** flip an image upside down about its bottom edge */
    public void verticalFlip()
    {
        for(int c = 0; c < pixels[0].length; c++)
        {
            for(int r = 0; r < pixels.length/2; r++)
            {
                Pixel temp = pixels[r][c];
                pixels[r][c] = pixels[pixels.length - r - 1][c];
                pixels[pixels.length - r - 1][c] = temp;
            }
        }
    }

    /** fix roof on greek temple */
    public void fixRoof()
    {
        for(int r = 30; r < pixels.length - 337; r++)
        {
            for(int c = 16; c < pixels[0].length - 284; c++)
            {
                pixels[r][pixels[0].length - c - 1] = pixels[r][c];
            }
        }
    }

    /** detect and mark edges in an image */
    public void edgeDetection(int dist)
    {
        Pixel newPixels[][] = new Pixel[pixels.length][pixels[0].length];
        for(int r = 0; r < newPixels.length; r++)
        {
            for(int c = 0; c < newPixels[0].length; c++)
            {
                newPixels[r][c] = new Pixel(255, 255, 255);
            }
        }
        
        for(int r = 1; r < pixels.length - 1; r++)
        {
            for(int c = 0; c < pixels[0].length - 1; c++)
            {
                //System.out.println(curDist);
                
                double f_Diff = pixels[r][c].colorDistance(pixels[r][c+1].getColor());
                double fu_Diff = pixels[r][c].colorDistance(pixels[r-1][c+1].getColor());
                double fd_Diff = pixels[r][c].colorDistance(pixels[r+1][c+1].getColor());
        
                
                if(f_Diff < 25)
                    newPixels[r][c+1].setColor(255, 255, 255);
                else
                    newPixels[r][c+1].setColor(0, 0, 0);
                        
                if(fu_Diff < 25)
                    newPixels[r-1][c+1].setColor(255, 255, 255);
                else 
                    newPixels[r-1][c+1].setColor(0, 0, 0);
                        
                if(fd_Diff < 25)
                    newPixels[r+1][c+1].setColor(255, 255, 255);
                else
                    newPixels[r+1][c+1].setColor(0, 0, 0);
                        
                f_Diff = 0;
                fu_Diff = 0;
                fd_Diff = 0;
            }
        }
        pixels = newPixels;
    }


    /** copy another picture's pixels into this picture, if a color is within dist of param Color */
    public void chromakey(Picture other, Color color, int dist)
    {
        Pixel pixels2[][] = other.getPixels();
        
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                if(pixels[r][c].colorDistance(color) < dist)
                {
                    pixels[r][c] = pixels2[r][c];
                }
            }  
        }
    }

    /** steganography encode (hide the message in msg in this picture) */
    public void encode(Picture msg)
    {
        Pixel pixels2[][] = msg.getPixels();

        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                int red = pixels[r][c].getRed();
                pixels[r][c].setRed(red - red%2);
                
                if(pixels2[r][c].getRed() < 30) //all the color values are the same so i couldve used red green or blue
                {
                    pixels[r][c].setRed(pixels[r][c].getRed()+1);
                }
            }
        }
    }

    /** steganography decode (return a new Picture containing the message hidden in this picture) */
    public Picture decode()
    {
        Picture message = new Picture(pixels.length, pixels[0].length);
        Pixel pixels2[][] = message.getPixels();
        
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                if(pixels[r][c].getRed()%2 == 1)
                {
                    pixels2[r][c].setColor(0, 0, 0);
                }
            }
        }
        
        return message;
    }

    /** perform a simple blur using the colors of neighboring pixels */
    public Picture simpleBlur()
    {
        return blur(1);
    }

    /** perform a blur using the colors of pixels within radius of current pixel */
    public Picture blur(int radius)
    {
        Picture blurredPic = new Picture(this);
        int totalArea = 0;
        
        
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                int avgRed = 0;
                int avgGreen = 0;
                int avgBlue = 0;
                
                for(int r2 = -radius; r2 < radius+1; r2++)
                {
                    for(int c2 = -radius; c2 < radius+1; c2++)
                    {
                        if(r+r2 < 0 || r+r2 > pixels.length-1)
                        {
                            r2++;
                            continue;
                        }
                        if(c+c2 < 0 || c+c2 > pixels[0].length-1)
                        {
                            c2++;
                            continue;
                        }
                        avgRed += pixels[r+r2][c+c2].getRed();
                        avgGreen += pixels[r+r2][c+c2].getGreen();
                        avgBlue += pixels[r+r2][c+c2].getBlue();
                        
                        totalArea++;
                    }
                }
                avgRed /= totalArea;
                avgGreen /= totalArea;
                avgBlue /= totalArea;
                
                totalArea = 0;
                blurredPic.setPixel(c, r, new Pixel(avgRed, avgGreen, avgBlue));
            }
        }
        
        return blurredPic;
    }
    
    /**
     * Simulate looking at an image through a pane of glass
     * @param dist the "radius" of the neighboring pixels to use
     * @return a new Picture with the glass filter applied
     */
    public Picture glassFilter(int dist) 
    {
        Picture glass = new Picture(pixels.length, pixels[0].length);
        for(int r = 0; r < pixels.length; r++)
        {
            for(int c = 0; c < pixels[0].length; c++)
            {
                int x2 = 0;
                int y2 = 0;
                while(true)
                {
                    int randX = random(dist);
                    if(r + randX >= 0 && r + randX < pixels.length)
                    {
                        x2 = r + randX;
                        break;
                    }
                }
                while(true)
                {
                    int randY = random(dist);
                    if(c + randY >= 0 && c + randY < pixels[0].length)
                    {
                        y2 = c + randY;
                        break;
                    }
                }
                Pixel newPixel = pixels[x2][y2];
                glass.setPixel(c, r, new Pixel(pixels[x2][y2].getColor()));
            }
        }
        return glass;
    }
    private int random(int dist)
    {
        int base = (int) (Math.random() * (dist * 2));
        int num = base - dist;
        return num;
    }
}

