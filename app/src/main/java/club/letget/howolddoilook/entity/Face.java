package club.letget.howolddoilook.entity;

/**
 * Created by mafei on 2015/8/18.
 */
public class Face {
    private int age;
    private String gender;
    private double centerX;
    private double centerY;
    private double height;
    private double width;
//    private int imgHeight;
//    private int imgWidth;


//    public int getImgHeight() {
//        return imgHeight;
//    }
//
//    public void setImgHeight(int imgHeight) {
//        this.imgHeight = imgHeight;
//    }
//
//    public int getImgWidth() {
//        return imgWidth;
//    }
//
//    public void setImgWidth(int imgWidth) {
//        this.imgWidth = imgWidth;
//    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getCenterX() {
        return centerX;
    }

    public void setCenterX(double centerX) {
        this.centerX = centerX;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getCenterY() {
        return centerY;
    }

    public void setCenterY(double centerY) {
        this.centerY = centerY;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "Face{" +
                "age=" + age +
                ", gender='" + gender + '\'' +
                ", centerX=" + centerX +
                ", centerY=" + centerY +
                ", height=" + height +
                ", width=" + width +
                '}';
    }
}
