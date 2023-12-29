package org.example;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import java.io.IOException;

@Listeners(CustomListeners.class)
public class Main {
    static WebDriver driver;
    static ExtentReports extent =  new ExtentReports();
    static DataFormatter formatter =  new DataFormatter();

    @BeforeTest
    public void setUp() throws IOException {
        System.setProperty("webdriver.chrome.driver", "D:\\seleniumjars\\chromedriver\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        String path = System.getProperty("user.dir")+"\\reports\\index.html";
        ExtentSparkReporter reporter = new ExtentSparkReporter(path);
        reporter.config().setReportName("ByteFactory - DPMS Automation Results");
        reporter.config().setDocumentTitle("Test Results");
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Vigneshkumar D");
    }

}
