package org.example;

import com.aventstack.extentreports.ExtentReports;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
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

@Listeners(CustomListeners.class)
public class ResolutionWorkOrderTest {
    WebDriver driver;
    ExtentReports extent;
    static String woNum;
    DataFormatter formatter;
    @BeforeTest
    public void setUpDriver(){
        driver = Main.driver;
        extent = Main.extent;
        formatter = Main.formatter;
    }

    public void getWoNum() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[5]")));
        schedulerElement.click();
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean hasNextPage = true;
        while (hasNextPage) {
            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement execNumElement;

                try {
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));

                }
                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);
                if (isValidExecNum) {
                    woNum = webElement.findElement(By.xpath(".//td[3]")).getText();
                    break;
                }
            }
//            try {
//                WebElement nextPageButton = driver.findElement(By.xpath("(//button[@class=\"ant-pagination-item-link\"])[2]"));
//                if (nextPageButton.isEnabled()) {
//                    nextPageButton.click();
//
//                } else {
//                    hasNextPage = false;
//                }
//            } catch (NoSuchElementException e) {
//                System.out.println("No next page button found. Exiting loop.");
//                hasNextPage = false;
//            }
            hasNextPage = false;
        }
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

    @DataProvider(name = "ResolutionWorkOrderData")
    public Object[][] getResolutionWorkOrderData() throws IOException {
        FileInputStream fis = new FileInputStream("C:\\Users\\vinay\\Downloads\\TestData.xlsx");
        XSSFWorkbook workbook= new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(4);
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

    public boolean testReports(String status){
        System.out.println(woNum);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[6]")));
        Actions actions = new Actions(driver);
        actions.moveToElement(schedulerElement).click().perform();
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
                    boolean isValidWoNum = woNumElement.getText().equalsIgnoreCase(woNum);
                    boolean isValidStatus = statusElement.getText().equalsIgnoreCase(status);
                    System.out.println("Element: "+woNumElement.getText() + " " + statusElement.getText());
                    if (isValidWoNum && isValidStatus) {
                        System.out.println("Element: "+woNumElement.getText() + " " + statusElement.getText());
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

    @Test(priority = 1)
    public void testManagerLogin() throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> userCredentials = getTestData("Login Manager");

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
        this.getWoNum();
    }

    @Test(priority = 2)
    public void testDashBoardOpenedStatus(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        schedulerElement.click();
        System.out.println("In DashBoard: " +ChecklistExecutionTest.execNum);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        WebElement openedElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Opened']")));
        openedElement.click();

        boolean hasNextPage = true;
        boolean foundText = false;
        while (hasNextPage) {

            List<WebElement> tableData =  driver.findElements(By.xpath("//tbody//tr"));

            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement statusElement;
                WebElement execNumElement;

                try {
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));

                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                }
                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);
                if (isValidExecNum) {
                    foundText = true;
                    break;
                }
            }
            hasNextPage = false;
        }
        Assert.assertTrue(foundText);
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Test(priority = 3)
    public void testReportOpenedStatus(){
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
        Assert.assertTrue(testReports("Opened"));
    }

    @Test(priority = 4, dataProvider = "ResolutionWorkOrderData")
    public void testResolutionWorkOrder(String assignedTo, String priority, String dueDate, String rca, String ca, String pa) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement reportElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[2]")));
        reportElement.click();
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[5]")));
        schedulerElement.click();
        System.out.println("In RWO: " +ChecklistExecutionTest.execNum);

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean hasNextPage = true;
//        boolean foundText = false;
        while (hasNextPage) {
            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            System.out.println(tableData.size());

            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement openElement;
                WebElement execNumElement;

                try {
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));

                    openElement = driver.findElement(By.xpath("//span[contains(text(),'Open')]"));
                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    openElement = driver.findElement(By.xpath("//span[contains(text(),'Open')]"));
                    webElement = tableData.get(i);
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));

                }
                System.out.println("Out Side Condition: "+"woNum:" + woNum + " ExecNum: ");
                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);
                if (isValidExecNum) {
//                    woNum = webElement.findElement(By.xpath(".//td[3]")).getText();
//                    System.out.println("woNum:" + woNum + " ExecNum: "+ChecklistExecutionTest.execNum);
                    openElement.click();
                    break;
                }
            }
            try {
                WebElement nextPageButton = driver.findElement(By.xpath("(//button[@class=\"ant-pagination-item-link\"])[2]"));
                if (nextPageButton.isEnabled()) {
                    nextPageButton.click(); // Click the next page button

                } else {
                    hasNextPage = false; // Set hasNextPage to false if there's no next page
                }
            } catch (NoSuchElementException e) {
                System.out.println("No next page button found. Exiting loop.");
                hasNextPage = false; // Exit loop if there's no next page button
            }
//            hasNextPage = false;
        }

        driver.findElement(By.xpath("//input[@id='assignedToId']")).click();
        try {
            boolean isVisible = false;
            WebElement desiredOption = null;
            do {
                Actions actions = new Actions(driver);
                actions.sendKeys(Keys.ARROW_DOWN).build().perform();
                desiredOption = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'" + assignedTo + "')]")));
            } while (!desiredOption.isDisplayed());
            desiredOption.click();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(assignedTo);
        driver.findElement(By.xpath("//input[@id='priority']")).click();
        WebElement priorityElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'" + priority + "')]")));
        priorityElement.click();

        driver.findElement(By.xpath("//input[@id='dueDate']")).click();
        WebElement dueDateElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[@class='ant-picker-today-btn']")));
        dueDateElement.click();

        driver.findElement(By.xpath("//span[normalize-space()='Next']")).click();
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//
//        driver.findElement(By.xpath("//textarea[@id='rca']")).sendKeys(rca);
//        driver.findElement(By.xpath("//textarea[@id='ca']")).sendKeys(ca);
//        driver.findElement(By.xpath("//textarea[@id='pa']")).sendKeys(pa);
//        driver.findElement(By.xpath("//span[normalize-space()='Send For Approval']")).click();
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        driver.findElement(By.xpath("//span[normalize-space()='Approve']")).click();
//
//        WebElement goToList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Go To List']")));
//        goToList.click();
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Test(priority = 5)
    public void testDashBoardAssignedStatus(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        schedulerElement.click();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        WebElement openedElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Assigned']")));
        openedElement.click();

        boolean hasNextPage = true;
        boolean foundText = false;
        while (hasNextPage) {

            List<WebElement> tableData =  driver.findElements(By.xpath("//tbody//tr"));
            System.out.println(tableData.size());

            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement execNumElement;

                try {
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));


                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                }
                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);
                if (isValidExecNum) {
                    foundText = true;
                    break;
                }
            }
            hasNextPage = false;
        }
        Assert.assertTrue(foundText);
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Test(priority = 6)
    public void testReportAssignedStatus(){
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
        Assert.assertTrue(testReports("Assigned"));

    }

    @Test(priority = 7)
    public void testLogout() throws IOException {
        ArrayList<String> userCredentials = getTestData("Login Manager");
        String username = userCredentials.get(1);
        driver.findElement(By.xpath("//strong[normalize-space()='" + username + "']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='Logout']")));
        logoutElement.click();
    }

    @Test(priority = 8)
    public void testSuccessfulLogin() throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<String> userCredentials = getTestData("Login AssignedTo User");
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

    @Test(priority = 9, dataProvider = "ResolutionWorkOrderData")
    public void testRWOExecution(String assignedTo, String priority, String dueDate, String rca, String ca, String pa) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[5]")));
        schedulerElement.click();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean hasNextPage = true;
        boolean foundText = false;
        while (hasNextPage) {

            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            System.out.println(tableData.size());

            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement openElement;
                WebElement execNumElement;

                try {
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                    openElement = driver.findElement(By.xpath("//span[contains(text(),'Open')]"));
                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    openElement = driver.findElement(By.xpath("//span[contains(text(),'Open')]"));
                    webElement = tableData.get(i);
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                }

                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);

                if (isValidExecNum) {
                    System.out.println("execNumElement:" + execNumElement.getText());
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
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            driver.findElement(By.xpath("//textarea[@id='rca']")).sendKeys(rca);
            driver.findElement(By.xpath("//textarea[@id='ca']")).sendKeys(ca);
            driver.findElement(By.xpath("//textarea[@id='pa']")).sendKeys(pa);
            driver.findElement(By.xpath("//span[normalize-space()='Send For Approval']")).click();
            hasNextPage = false;
        }

        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }

    }

    @Test(priority = 10)
    public void testDashBoardResolvedStatus(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        schedulerElement.click();

        WebElement openedElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Resolved']")));
        openedElement.click();

        boolean hasNextPage = true;
        boolean foundText = false;
        while (hasNextPage) {

            List<WebElement> tableData =  driver.findElements(By.xpath("//tbody//tr"));
            System.out.println(tableData.size());

            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement execNumElement;

                try {
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));

                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                }
                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);
                if (isValidExecNum) {
                    foundText = true;
                    break;
                }
            }
            hasNextPage = false;
        }
        Assert.assertTrue(foundText);
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Test(priority = 11)
    public void testReportResolvedStatus(){
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
        Assert.assertTrue(testReports("Resolved"));
    }

    @Test(priority = 12)
    public void testLogoutAssignedToUser() throws IOException {
        ArrayList<String> userCredentials = getTestData("Login AssignedTo User");
        String username = userCredentials.get(1);
        driver.findElement(By.xpath("//strong[normalize-space()='" + username + "']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='Logout']")));
        logoutElement.click();
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Test(priority = 13)
    public void testAssignedByLogin() throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> userCredentials = getTestData("Login Manager");

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
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Test(priority = 14)
    public void testApproval() {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement schedulerElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[5]")));
        schedulerElement.click();

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean hasNextPage = true;
        boolean foundText = false;
        while (hasNextPage) {

            List<WebElement> tableData = driver.findElements(By.xpath("//tbody//tr"));
            System.out.println(tableData.size());

            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement openElement;
                WebElement execNumElement;

                try {
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                    openElement = driver.findElement(By.xpath("//span[contains(text(),'Open')]"));
                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    openElement = driver.findElement(By.xpath("//span[contains(text(),'Open')]"));
                    webElement = tableData.get(i);
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                }

                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);

                if (isValidExecNum) {
                    System.out.println("execNumElement:" + execNumElement.getText());
                    foundText = true;
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

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            driver.findElement(By.xpath("//span[normalize-space()='Approve']")).click();

            WebElement goToList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Go To List']")));
            goToList.click();
        }
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Test(priority = 15)
    public void testReportCompletedStatus(){
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
        Assert.assertTrue(testReports("Completed"));

    }

    @Test(priority = 16)
    public void testTicketStatus(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[5]")));
        resolutionWorkOrder.click();
        boolean hasNextPage = true;
        boolean foundText = false;
        while (hasNextPage) {

            List<WebElement> tableData =  driver.findElements(By.xpath("//tbody//tr"));
            System.out.println(tableData.size());

            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement statusElement;
                WebElement execNumElement;

                try {
                    statusElement = webElement.findElement(By.xpath(".//td[9]"));
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));

                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    statusElement = webElement.findElement(By.xpath(".//td[9]"));
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));
                }

                boolean isValidStatus = statusElement.getText().equalsIgnoreCase("Completed");
                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);

                if (isValidExecNum&&isValidStatus) {
                    foundText = true;
                    break;
                }
            }
            hasNextPage = false;
        }
        Assert.assertTrue(foundText);
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

    @Test(priority = 17)
    public void testDashBoardCompletedStatus(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement resolutionWorkOrder = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[1]")));
        resolutionWorkOrder.click();

        driver.findElement(By.xpath("//span[normalize-space()='Completed']")).click();

        boolean hasNextPage = true;
        boolean foundText = false;
        while (hasNextPage) {

            List<WebElement> tableData =  driver.findElements(By.xpath("//tbody//tr"));
            System.out.println(tableData.size());

            for (int i = 0; i < tableData.size(); i++) {
                WebElement webElement = tableData.get(i);
                WebElement statusElement;
                WebElement execNumElement;

                try {
                    statusElement = webElement.findElement(By.xpath(".//td[9]"));
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));


                } catch (StaleElementReferenceException e) {
                    System.out.println("Stale element reference encountered");
                    try {
                        Thread.sleep(3000);
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                    tableData = driver.findElements(By.xpath("//tbody//tr"));
                    webElement = tableData.get(i);
                    statusElement = webElement.findElement(By.xpath(".//td[9]"));
                    execNumElement = webElement.findElement(By.xpath(".//td[2]"));

                }

                boolean isValidStatus = statusElement.getText().equalsIgnoreCase("Completed");
                boolean isValidExecNum = execNumElement.getText().equalsIgnoreCase(ChecklistExecutionTest.execNum);

                if (isValidStatus && isValidExecNum) {
                    foundText = true;
                    break;
                }
            }
            hasNextPage = false;
        }
        Assert.assertTrue(foundText);
        try {
            Thread.sleep(3000);
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }
}
