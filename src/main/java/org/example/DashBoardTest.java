package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

@Listeners(CustomListeners.class)
public class DashBoardTest {
    WebDriver driver;
    String execNum;
    @BeforeTest
    public void setUpDriver(){
        driver = Main.driver;
        execNum = ChecklistExecutionTest.execNum;
    }

    @Test(priority = 1)
    public void testOverallTicketCount(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();
        WebElement ticketCount = driver.findElement(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[3]"));
        System.out.println("Ticket: "+ticketCount.getText());
    }

    @Test(priority = 2)
    public void testCheckListExecutionStatusScheduled(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();
        WebElement scheduledCount = driver.findElement(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[11]"));
        System.out.println("Scheduled: "+scheduledCount.getText());
    }

    @Test(priority = 3)
    public void testCheckListExecutionStatusInProgress(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();
        WebElement inProgressCount = driver.findElement(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[12]"));
        System.out.println("InProgress: "+inProgressCount.getText());
    }

    @Test(priority = 4)
    public void testCheckListExecutionStatusClosed(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();
        WebElement closedCount = driver.findElement(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[13]"));
        System.out.println("Closed: "+closedCount.getText());
    }
}
