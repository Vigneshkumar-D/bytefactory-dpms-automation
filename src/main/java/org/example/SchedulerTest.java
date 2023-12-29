package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@Listeners(CustomListeners.class)
public class SchedulerTest {
    WebDriver driver;
    DataFormatter formatter;
    String execNum;

    @BeforeTest
    public void setUpDriver() {
        driver = Main.driver;
        formatter = Main.formatter;
        execNum = ChecklistExecutionTest.execNum;
    }

    public ArrayList<String>  getTestData(String testCase) throws IOException {
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

    @DataProvider(name = "SchedulerData")
    public Object[][] getUserData() throws IOException {
        FileInputStream fis = new FileInputStream("C:\\Users\\vinay\\Downloads\\TestData.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(3);
        int rowCount = sheet.getPhysicalNumberOfRows();
        XSSFRow row = sheet.getRow(0);
        int columnCount = row.getLastCellNum();
        Object[][] data = new Object[rowCount - 1][columnCount];
        for (int i = 0; i < rowCount - 1; i++) {
            row = sheet.getRow(i + 1);
            for (int j = 0; j < columnCount; j++) {
                XSSFCell cell = row.getCell(j);
                data[i][j] = formatter.formatCellValue(cell);
            }
        }
        return data;
    }

    @Test(priority = 1)
    public void testManagerLogin() throws IOException {
        ArrayList<String> userCredentials = getTestData("Login Manager");
        String userName = userCredentials.get(1);
        String password = userCredentials.get(2);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement usernameInput2 = driver.findElement(By.id("basic_userName"));
        WebElement passwordInput2 = driver.findElement(By.id("basic_password"));
        WebElement loginButton2 = driver.findElement(By.cssSelector(".ant-btn.css-ru2fok.ant-btn-primary.ant-btn-block"));

        usernameInput2.sendKeys(userName);
        passwordInput2.sendKeys(password);
        loginButton2.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));
        wait.until(ExpectedConditions.urlToBe("http://192.168.0.189:4000/dashboard"));
        String expectedUrlAfterLogin = "http://192.168.0.189:4000/dashboard";
        String actualUrlAfterLogin = driver.getCurrentUrl();
        Assert.assertEquals(actualUrlAfterLogin, expectedUrlAfterLogin);
    }

    @Test(priority = 2, dataProvider = "SchedulerData")
    public void testSchedulerCreation(String assertName, String checkList, String frequency, String assignedToUser, String startDate, String endDate, String startTime, String endTime, String description) {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(50));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[3]")));
        schedulerElement.click();

        WebElement today = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='rbc-day-bg rbc-today']")));
        Actions actions = new Actions(driver);
        actions.moveToElement(today).click().perform();

        WebElement assertInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='assetId']")));
        assertInput.click();

        WebElement assertNameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'" + assertName + "')]")));
        assertNameElement.click();

        driver.findElement(By.xpath("//div[@class='ant-select-selection-overflow']")).click();
        WebElement checkListInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'" + checkList + "')]")));
        checkListInput.click();

        driver.findElement(By.xpath("//input[@id='frequency']")).click();
        WebElement frequencyInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ant-select-item-option-content'][normalize-space()='" + frequency + "']")));
        frequencyInput.click();

        WebElement assignedTo = driver.findElement(By.xpath("//input[@id='userId']"));
        assignedTo.click();
        assignedTo.sendKeys(assignedToUser);

        WebElement assignedToUserInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'" + assignedToUser + "')]")));
        assignedToUserInput.click();

        driver.findElement(By.xpath("//input[@id='scheduleDate']")).click();
        WebElement startDateInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@class='ant-picker-content']")));
        startDateInput.click();

        driver.findElement(By.xpath("//input[@id='startTime']")).click();
        WebElement startTimeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ant-picker-dropdown css-ru2fok ant-picker-dropdown-placement-bottomLeft ']//a[@class='ant-picker-now-btn'][normalize-space()='Now']")));
        startTimeInput.click();

        WebElement endTimeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='endTime']")));
        endTimeInput.click();
        endTimeInput.sendKeys(endTime);

        WebElement okInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@class='ant-picker-dropdown css-ru2fok ant-picker-dropdown-placement-bottomLeft ']//li[@class='ant-picker-ok'][normalize-space()='OK']")));
        okInput.click();

        driver.findElement(By.xpath("//textarea[@id='description']")).sendKeys(description);
        driver.findElement(By.xpath("//span[normalize-space()='Save']")).click();

        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }

        WebElement dayButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Day']")));
        actions = new Actions(driver);
        actions.moveToElement(dayButton).click().perform();

        boolean foundText = false;
        List<WebElement> todaySchedulers = driver.findElements(By.xpath("//div[@class='rbc-events-container']/div"));

        for (WebElement webElement : todaySchedulers) {
            try {
                WebElement subElement = webElement.findElement(By.xpath(".//h3[@style='margin-bottom: 1px;']"));
                if (subElement.getText().equalsIgnoreCase(assignedToUser)) {
                    foundText = true;
                    break;
                }
            } catch (NoSuchElementException e) {
                System.out.println("h3 element not found within this div");
            }
        }
        Assert.assertTrue(foundText);
    }

    @Test(priority = 3)
    public void testCheckListExecutionStatusScheduled(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement dashboardEle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        dashboardEle.click();
        WebElement scheduledCountEle = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[8]")));
        String scheduledCount = scheduledCountEle.getText();
        Actions actions = new Actions(driver);
        actions.moveToElement(scheduledCountEle).click().perform();
        int checklistExecutionScheduled = 0;
        try{
            Thread.sleep(3000);
        }catch (Exception e){
            e.printStackTrace();
        }

        boolean hasNextPage = true;
        while (hasNextPage) {
            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            if(tableData.size() == 1){
                checklistExecutionScheduled = 0;
            }else{
                checklistExecutionScheduled += tableData.size()-1;
            }
            try {
                WebElement nextPageButton = driver.findElement(By.xpath("(//button[@class=\"ant-pagination-item-link\"])[2]"));
                if (nextPageButton.isEnabled()) {
                    nextPageButton.click();
                } else {
                    hasNextPage = false;
                }
            } catch (org.openqa.selenium.NoSuchElementException e) {
                System.out.println("No next page button found. Exiting loop.");
                hasNextPage = false;
            }
        }
    Assert.assertEquals(checklistExecutionScheduled, Integer.parseInt(scheduledCount));
    }
}