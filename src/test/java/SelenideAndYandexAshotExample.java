import com.codeborne.selenide.SelenideElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;


@Test
public class SelenideAndYandexAshotExample {
    static Path tempDirectory;

    @BeforeClass
    protected void before() throws Throwable {
        System.out.println("Setting Chromium as a default browser.");
        tempDirectory = Files.createTempDirectory("test-ashot-directory");
    }

    @Test(groups = {"selenium"}, dataProviderClass = DataProviders.class, dataProvider = "loadBankNamesFromFile")
    public void getNewsLinksData(String currentBankName) {
        try {
                saveStellaImage("banki.ru", "/products/debitcards/", currentBankName);
                saveStellaImage("banki.ru", "/products/creditcards/", currentBankName);

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }

    private void saveStellaImage(String envDomainName, String StellaSuffix, String BankCode) throws IOException {

        String FileName = BankCode + "_" + StellaSuffix.replace('/', '_') + ".png";
        String outputfileName = tempDirectory.toString() + "/" + FileName;
        System.out.println("TMP dir: " + tempDirectory.toString());

        if (checkExistingStellaImages(BankCode, FileName, outputfileName)) return;
        String NewsUrl = "http://" + envDomainName + StellaSuffix + BankCode + "/";
        getStellaImages(outputfileName, NewsUrl);
    }

    private void getStellaImages(String outputfileName, String newsUrl) {
        try {
            do {
                open(newsUrl);

            }
            while (!$("#main-menu").exists());

            BufferedImage bi = new BufferedImage(256, 256,
                    BufferedImage.TYPE_INT_RGB);
            String StellaAllocatorCSS = "[data-test*=\"banks-stella\"]";
            if ($(StellaAllocatorCSS).exists()) {
                SelenideElement el = $(StellaAllocatorCSS);
                Screenshot as = new AShot().takeScreenshot(getWebDriver(), el);
                bi = as.getImage();
            }
            File outputfile = new File(outputfileName);
            ImageIO.write(bi, "png", outputfile);
        } catch (
                IOException e
                ) {
        }
    }

    private boolean checkExistingStellaImages(String BankCode, String fileName, String outputfileName) {
        File f = new File(outputfileName);
        if (f.exists() && !f.isDirectory()) {
            File f_alt_1 = new File(outputfileName.replace("debitcards", "creditcards"));
            File f_alt_2 = new File(outputfileName.replace("creditcards", "debitcards"));
            if ((f_alt_1.exists() && !f_alt_1.isDirectory()) && (f_alt_2.exists() && !f_alt_2.isDirectory())) {
                try {

                    BufferedImage creditCardImage = ImageIO.read(f_alt_1);
                    BufferedImage debitCardImage = ImageIO.read(f_alt_2);

                    ImageDiff diff = new ImageDiffer().makeDiff(creditCardImage, debitCardImage);
                    if (diff.hasDiff()) {
                        String diffFlolder = tempDirectory + "/diff/";

                        BufferedImage diffImage = diff.getMarkedImage();
                        File outputfile = new File(diffFlolder + BankCode + "_diff" + ".png");
                        ImageIO.write(diffImage, "png", outputfile);

                        File creditCardsFile = new File(diffFlolder + fileName.replace("debitcards", "creditcards"));
                        ImageIO.write(creditCardImage, "png", creditCardsFile);

                        File debitCardsFile = new File(diffFlolder + fileName.replace("creditcards", "debitcards"));
                        ImageIO.write(debitCardImage, "png", debitCardsFile);
                    }
                } catch (
                        IOException e
                        ) {
                }
            }
            // do something
            return true;
        }
        return false;
    }
}
