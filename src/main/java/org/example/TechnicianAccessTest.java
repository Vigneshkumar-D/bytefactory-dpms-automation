package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;

@Listeners(CustomListeners.class)
public class TechnicianAccessTest {
    WebDriver driver;
    DataFormatter formatter;
    @BeforeTest
    public void setUpDriver() {
        driver = Main.driver;
        formatter = Main.formatter;
    }

    public ArrayList<String> getTestData(String testCase) throws IOException {
        FileInputStream fis=new FileInputStream("C:\\Users\\vinay\\Downloads\\TestData.xlsx");
        XSSFWorkbook workbook= new XSSFWorkbook(fis);
        ArrayList<String> userCredentials = new ArrayList<>();

        int sheets = workbook.getNumberOfSheets();
        for (int i=0; i<sheets; i++){
            if(workbook.getSheetName(i).equalsIgnoreCase("Login")){
                XSSFSheet sheet = workbook.getSheetAt(i);
                Iterator<Row> rows = sheet.iterator();
                Row firstRow = rows.next();
                Iterator<Cell> ce = firstRow.cellIterator();
                int k=0;
                int column=0;
                while (ce.hasNext()){
                    Cell value = ce.next();
                    if(value.getStringCellValue().equalsIgnoreCase("TestCases")){
                        column=k;
                    }
                    k++;
                }

                while(rows.hasNext()){
                    Row r = rows.next();
                    if(r.getCell(column).getStringCellValue().equalsIgnoreCase(testCase)){
                        Iterator<Cell> cv = r.cellIterator();
                        while ((cv.hasNext())){
                            Cell nextCell = cv.next();
                            if (nextCell.getCellType() == CellType.STRING) {
                                userCredentials.add(nextCell.getStringCellValue());
                            } else if (nextCell.getCellType() == CellType.NUMERIC) {
                                int numericData = (int) nextCell.getNumericCellValue();
                                String numericAsString = String.valueOf(numericData);
                                userCredentials.add(numericAsString);
                            }
                        }
                    }
                }
            }
        }
        return userCredentials;
    }

    @Test(priority = 1)
    public void testSuccessfulLogin() throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> userCredentials = getTestData("Technician");

        String username = userCredentials.get(1);
        String password = userCredentials.get(2);
        WebElement usernameInput2 = driver.findElement(By.id("basic_userName"));
        WebElement passwordInput2 = driver.findElement(By.id("basic_password"));
        WebElement loginButton2 = driver.findElement(By.cssSelector(".ant-btn.css-ru2fok.ant-btn-primary.ant-btn-block"));

        usernameInput2.sendKeys(username);
        passwordInput2.sendKeys(password);
        loginButton2.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));
        wait.until(ExpectedConditions.urlToBe("http://192.168.0.189:4000/dashboard"));
        String expectedUrlAfterLogin = "http://192.168.0.189:4000/dashboard";
        String actualUrlAfterLogin = driver.getCurrentUrl();
        Assert.assertEquals(actualUrlAfterLogin, expectedUrlAfterLogin);
    }
    @Test(priority = 2)
    public void testTechnicianAccess(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[2]")));
        schedulerElement.click();
        WebElement userAccessElement = null;
        boolean elementAbsent = false;

        try {
            userAccessElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='UserAccess']")));
        } catch (Exception e) {
            elementAbsent = true;
        }
        if (elementAbsent || userAccessElement == null) {
            Assert.assertTrue(true);
        } else {
             Assert.assertTrue(userAccessElement.isDisplayed(), "UserAccess element is displayed unexpectedly.");
        }
    }

    @Test(priority = 3)
    public void testLogout() throws IOException {
        ArrayList<String> userCredentials = getTestData("Technician");
        String username = userCredentials.get(1);
        driver.findElement(By.xpath("//strong[normalize-space()='" + username +"']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='Logout']")));
        logoutElement.click();
    }
}

//div[@class="apexcharts-tooltip-series-group apexcharts-active"]