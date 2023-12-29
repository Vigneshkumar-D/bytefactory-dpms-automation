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
import org.openqa.selenium.StaleElementReferenceException;
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
public class ChecklistExecutionTest {
    WebDriver driver;
    static String execNum;
    DataFormatter formatter;
    int ticketCount = 0;
    @BeforeTest
    public void setUpDriver(){
        driver = Main.driver;
        formatter = Main.formatter;
    }

    @DataProvider(name = "SchedulerData")
    public Object[][] getUserData() throws IOException {
        FileInputStream fis = new FileInputStream("C:\\Users\\vinay\\Downloads\\TestData.xlsx");
        XSSFWorkbook workbook= new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(3);
        int rowCount = sheet.getPhysicalNumberOfRows();
        XSSFRow row = sheet.getRow(0);
        int columnCount = row.getLastCellNum();
        Object[][] data = new Object[rowCount-1][columnCount];
        for(int i=0; i<rowCount-1; i++){
            row = sheet.getRow(i+1);
            for(int j=0; j<columnCount; j++){
                XSSFCell cell = row.getCell(j);
                data[i][j] = formatter.formatCellValue(cell);
            }
        }
        return data;
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

    public void actualTicketCount(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();
        WebElement ticketCountElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[2]")));
        String ticketCountText = ticketCountElement.getText();
        ticketCount  = Integer.parseInt(ticketCountText);
    }

    @Test(priority = 1, dataProvider = "SchedulerData")
    public void testChecklistExecution(String assertName, String checkList, String frequency, String assignedToUser, String startDate, String endDate, String startTime, String endTime, String description) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[4]")));
        schedulerElement.click();
        Assert.assertTrue(this.validateChecklistExecution(assignedToUser, assertName, description));
    }

    public Boolean validateChecklistExecution(String assignedToUser, String assertName, String description) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        boolean hasNextPage = true;
        boolean foundText = false;

        while (hasNextPage) {
            List<WebElement> tableData =  driver.findElements(By.xpath("//tbody//tr"));
            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement assignedToElement;
                WebElement descriptionElement;
                WebElement assertNameElement;

                try {
                    descriptionElement = webElement.findElement(By.xpath(".//td[5]"));
                    assertNameElement = webElement.findElement(By.xpath(".//td[6]"));
                    assignedToElement = webElement.findElement(By.xpath(".//td[7]"));
//                    execNum = webElement.findElement(By.xpath(".//td[2]")).getText();

                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    descriptionElement = webElement.findElement(By.xpath(".//td[5]"));
                    assertNameElement = webElement.findElement(By.xpath(".//td[6]"));
                    assignedToElement = webElement.findElement(By.xpath(".//td[7]"));

                }
                Boolean isValidRole = assignedToElement.getText().equalsIgnoreCase(assignedToUser);
                Boolean isValidDes = descriptionElement.getText().equalsIgnoreCase(description);
                Boolean isValidAssert = assertNameElement.getText().equalsIgnoreCase(assertName);

                if (isValidRole && isValidDes && isValidAssert) {
                    execNum = webElement.findElement(By.xpath(".//td[2]")).getText();
                    System.out.println(execNum);
                    foundText = true;
                    break;
                }
            }
//            try {
//                WebElement nextPageButton = driver.findElement(By.xpath("(//button[@class=\"ant-pagination-item-link\"])[2]"));
//                if (nextPageButton.isEnabled()) {
//                    nextPageButton.click();
//                } else {
//                    hasNextPage = false;
//                }
//            } catch (NoSuchElementException e) {
//                System.out.println("No next page button found. Exiting loop.");
                hasNextPage = false;
//            }
        }
        return foundText;
    }

    @Test(priority = 2)
    public void testLogout() throws IOException {
        ArrayList<String> userCredentials = getTestData("Login Manager");
        String username = userCredentials.get(1);
        driver.findElement(By.xpath("//strong[normalize-space()='" + username + "']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='Logout']")));
        logoutElement.click();
    }

    @Test(priority = 3)
    public void testSuccessfulLogin() throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> userCredentials = getTestData("Login Technician");

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
        this.actualTicketCount();
        Assert.assertEquals(actualUrlAfterLogin, expectedUrlAfterLogin);

    }

    @Test(priority = 4, dataProvider = "SchedulerData")
    public void testSchedulerExecutionInProgress(String assertName, String checkList, String frequency, String assignedToUser, String startDate, String endDate, String startTime, String endTime, String description) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[4]")));
        schedulerElement.click();
        boolean hasNextPage = true;
        boolean foundText = false;

        while (hasNextPage) {
            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement assignedToElement;
                WebElement descriptionElement;
                WebElement assertNameElement;
                WebElement openElement;
                try {
                    Thread.sleep(3000);
                }catch (Exception e1){
                    e1.printStackTrace();
                }

                try {
                    descriptionElement = webElement.findElement(By.xpath(".//td[5]"));
                    assignedToElement = webElement.findElement(By.xpath(".//td[7]"));
                    assertNameElement = webElement.findElement(By.xpath(".//td[6]"));
                    openElement = webElement.findElement(By.xpath("//span[contains(text(),'Open')]"));
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    System.out.println("Stale element reference encountered");
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    descriptionElement = webElement.findElement(By.xpath(".//td[5]"));
                    assignedToElement = webElement.findElement(By.xpath(".//td[7]"));
                    assertNameElement = webElement.findElement(By.xpath(".//td[6]"));
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    openElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(),'Open')]")));
                }

                Boolean isValidRole = assignedToElement.getText().equalsIgnoreCase(assignedToUser);
                Boolean isValidDes = descriptionElement.getText().equalsIgnoreCase(description);
                Boolean isValidAssert = assertNameElement.getText().equalsIgnoreCase(assertName);

                if (isValidRole && isValidDes && isValidAssert) {
                    openElement.click();
                    break;
                }
            }
//            try {
//                WebElement nextPageButton = driver.findElement(By.xpath("(//button[@class=\"ant-pagination-item-link\"])[2]"));
//                if (nextPageButton.isEnabled()) {
//                    nextPageButton.click(); // Click the next page button
//
//                } else {
//                    hasNextPage = false; // Set hasNextPage to false if there's no next page
//                }
//            } catch (NoSuchElementException e) {
//                System.out.println("No next page button found. Exiting loop.");
//                hasNextPage = false; // Exit loop if there's no next page button
//            }
            hasNextPage = false;
        }

        WebElement startButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Start']")));
        startButton.click();
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }

    }

    @Test(priority = 5)
    public void testCheckListExecutionStatusInProgress(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();
        WebElement inProgressEle= wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[9]")));
        String inProgressCount = inProgressEle.getText();
        Actions actions = new Actions(driver);
        actions.moveToElement(inProgressEle).click().perform();
        int checklistExecutionInProgress = 0;
        try{
            Thread.sleep(3000);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean hasNextPage = true;
        while (hasNextPage) {
            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            checklistExecutionInProgress += tableData.size()-1;
            if(tableData.size() == 1){
                checklistExecutionInProgress = 0;
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
        Assert.assertEquals(checklistExecutionInProgress, Integer.parseInt(inProgressCount));
    }

    @Test(priority = 6, dataProvider = "SchedulerData")
    public void testSchedulerExecutionClosed(String assertName, String checkList, String frequency, String assignedToUser, String startDate, String endDate, String startTime, String endTime, String description) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[4]")));
        schedulerElement.click();
        boolean hasNextPage = true;
        boolean foundText = false;

        while (hasNextPage) {
            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement assignedToElement;
                WebElement descriptionElement;
                WebElement assertNameElement;
                WebElement openElement;
                try {
                    Thread.sleep(3000);
                }catch (Exception e1){
                    e1.printStackTrace();
                }
                try {
                    descriptionElement = webElement.findElement(By.xpath(".//td[5]"));
                    assignedToElement = webElement.findElement(By.xpath(".//td[7]"));
                    assertNameElement = webElement.findElement(By.xpath(".//td[6]"));
                    openElement = webElement.findElement(By.xpath("//span[contains(text(),'Open')]"));
                } catch (StaleElementReferenceException | NoSuchElementException e) {
                    System.out.println("Stale element reference encountered");
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    descriptionElement = webElement.findElement(By.xpath(".//td[5]"));
                    assignedToElement = webElement.findElement(By.xpath(".//td[7]"));
                    assertNameElement = webElement.findElement(By.xpath(".//td[6]"));
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    openElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(),'Open')]")));
                }

                Boolean isValidRole = assignedToElement.getText().equalsIgnoreCase(assignedToUser);
                Boolean isValidDes = descriptionElement.getText().equalsIgnoreCase(description);
                Boolean isValidAssert = assertNameElement.getText().equalsIgnoreCase(assertName);

                if (isValidRole && isValidDes && isValidAssert) {
                    openElement.click();
                    this.updateCheckList();
                    break;
                }
            }
//            try {
//                WebElement nextPageButton = driver.findElement(By.xpath("(//button[@class=\"ant-pagination-item-link\"])[2]"));
//                if (nextPageButton.isEnabled()) {
//                    nextPageButton.click(); // Click the next page button
//
//                } else {
//                    hasNextPage = false; // Set hasNextPage to false if there's no next page
//                }
//            } catch (NoSuchElementException e) {
//                System.out.println("No next page button found. Exiting loop.");
//                hasNextPage = false; // Exit loop if there's no next page button
//            }
            hasNextPage = false;
        }

        try {
            Thread.sleep(1000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    public void updateCheckList(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        WebElement startButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Start']")));
//        startButton.click();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<WebElement> elementList = driver.findElements(By.xpath("//table[@class='table table-stripped']//tbody/tr"));

        for(int i=0; i<elementList.size(); i++){

            String xpathExpressionStatus = "//div[contains(@id, 'checks_" + i + "_status')]//span[contains(text(),'Yes')]";
            String xpathExpressionRemark = ".//textarea[@id='checks_"+i+"_remark']";
            if(i==elementList.size()-1){
                xpathExpressionStatus = "//div[contains(@id, 'checks_" + i + "_status')]//span[contains(text(),'No')]";
                driver.findElement(By.xpath(xpathExpressionStatus)).click();
                driver.findElement(By.xpath(xpathExpressionRemark)).sendKeys("Testing Data");
            }else{
                driver.findElement(By.xpath(xpathExpressionStatus)).click();
                driver.findElement(By.xpath(xpathExpressionRemark)).sendKeys("Testing Data");
            }
        }

        WebElement savePreviewEle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Save Preview']")));
        savePreviewEle.click();

        WebElement raiseTicket = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Raise Ticket(s)']")));
        raiseTicket.click();
    }

    @Test(priority = 7)
    public void testCheckListExecutionStatusClosed(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement configEle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[2]")));
        configEle.click();
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();
        WebElement closedCountEle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[10]")));
        String closedCount = closedCountEle.getText();
        Actions actions = new Actions(driver);
        actions.moveToElement(closedCountEle).click().perform();
        int checklistExecutionClosed = 0;
        try{
            Thread.sleep(3000);
        }catch (Exception e){
            e.printStackTrace();
        }
        boolean hasNextPage = true;
        while (hasNextPage) {
            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            checklistExecutionClosed += tableData.size()-1;
            if(tableData.size()==1){
                checklistExecutionClosed = 0;
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
        System.out.println(checklistExecutionClosed+" "+ Integer.parseInt(closedCount));
        Assert.assertEquals(checklistExecutionClosed, Integer.parseInt(closedCount));
    }

    @Test(priority = 8)
    public void testOverallAfterTicketCount(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();
        WebElement ticketCountElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//*[contains(@class, 'apexcharts-data-labels') and contains(@id, 'Svg')])[2]")));
        String ticketCountText = ticketCountElement.getText();
        int updatedTicketCount = Integer.parseInt(ticketCountText);
        Assert.assertEquals(ticketCount, updatedTicketCount);
    }

    @Test(priority = 9)
    public void testTechnicianLogout() throws IOException {
        ArrayList<String> userCredentials = getTestData("Login Technician");
        String username = userCredentials.get(1);
        driver.findElement(By.xpath("//strong[normalize-space()='" + username + "']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='Logout']")));
        logoutElement.click();
    }
}

