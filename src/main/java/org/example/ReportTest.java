package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;

//@Listeners(CustomListeners.class)
public class ReportTest {
//    WebDriver driver;
//    String woNum;
//
//    @BeforeTest
//    public void setUpDriver(){
//        driver = Main.driver;
//        woNum = ResolutionWorkOrderTest.woNum;
//    }

    public static boolean testReports(WebDriver driver, String woNum, String status){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[6]")));
        schedulerElement.click();
        boolean hasNextPage = true;
        boolean foundText = false;

        while (hasNextPage) {
            List<WebElement> tableData =  driver.findElements(By.xpath("//tbody//tr"));
            try {
                for (int i = 0; i < tableData.size(); i++) {
                    WebElement webElement = tableData.get(i);
                    WebElement woNumElement;
                    WebElement statusElement;

                    try {
                        woNumElement = webElement.findElement(By.xpath(".//td[2]"));
                        statusElement = webElement.findElement(By.xpath(".//td[6]"));
                    } catch (StaleElementReferenceException e) {
                        System.out.println("Stale element reference encountered");
                        tableData = driver.findElements(By.xpath("//tbody//tr"));
                        webElement = tableData.get(i);
                        try {
                            Thread.sleep(3000);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        woNumElement = webElement.findElement(By.xpath(".//td[2]"));
                        statusElement = webElement.findElement(By.xpath(".//td[6]"));
                    }

                    System.out.println("Element: "+woNumElement + " " + statusElement);
                    boolean isValidWoNum = woNumElement.getText().equalsIgnoreCase(woNum);
                    boolean isValidStatus = statusElement.getText().equalsIgnoreCase(status);
                    if (isValidWoNum && isValidStatus) {
                        foundText = true;
                        break;
                    }
                }
                hasNextPage = false;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return  foundText;
    }

//    @AfterTest
//    public void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
}
