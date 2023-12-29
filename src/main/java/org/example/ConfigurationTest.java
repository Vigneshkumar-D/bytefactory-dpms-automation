package org.example;

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
import java.util.NoSuchElementException;


@Listeners(CustomListeners.class)
public class ConfigurationTest {
    WebDriver driver;
    DataFormatter formatter;

    @BeforeTest
    public void setUpDriver(){
        driver = Main.driver;
        formatter = Main.formatter;
    }

    @DataProvider(name = "UserData1")
    public Object[][] getUserData1() {
        return getUserDataForTest(0);
    }

    @DataProvider(name = "UserData2")
    public Object[][] getUserData2() {
        return getUserDataForTest(1);
    }
    @DataProvider(name = "UserData3")
    public Object[][] getUserData3() {
        return getUserDataForTest(2);
    }

    @DataProvider(name = "combinedUserData")
    public Object[][] combineUserData() {
        Object[][] userData2 = getUserData2();
        Object[][] userData3 = getUserData3();

        Object[][] combinedData = new Object[userData2.length + userData3.length][3];
        System.arraycopy(userData2, 0, combinedData, 0, userData2.length);
        System.arraycopy(userData3, 0, combinedData, userData2.length, userData3.length);

        return combinedData;
    }

    private Object[][] getUserDataForTest(int rowIndex) {
        try {
            FileInputStream fis = new FileInputStream("C:\\Users\\vinay\\Downloads\\TestData.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(1); // Adjust the sheet index as needed
            int columnCount = sheet.getRow(0).getLastCellNum();
            Object[][] data = new Object[1][columnCount];

            XSSFRow row = sheet.getRow(rowIndex + 1);
            for (int j = 0; j < columnCount; j++) {
                XSSFCell cell = row.getCell(j);
                data[0][j] = formatter.formatCellValue(cell);
            }

            workbook.close(); // Close the workbook to release resources
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new Object[][]{}; // Return empty array if an exception occurs
        }
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

    @Test(priority = 1)
    public void testRoleCreation() throws IOException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        ArrayList<String> roleData = getTestData("Role Creation");
        String superUser = roleData.get(1);

        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[2]")));
        element.click();
        WebElement element1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='ant-tabs-nav-list'])[1]//div[1]//div//a")));
        element1.click();
        WebElement element2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='ant-row ant-row-space-between css-ru2fok'])//div[2]")));
        element2.click();

        WebElement roleNameInput1 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='roleName']")));
        roleNameInput1.sendKeys(superUser);

        WebElement button1 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
        button1.click();

        Boolean isSuperRoleAdded = validateRole(superUser);
        Assert.assertTrue(isSuperRoleAdded, "Expected text 'Test User' not found in any element");
    }

    @Test(priority = 2, dataProvider = "UserData1")
    public void testSuperUserCreation(String userName, String role, String email, String mobileNum, String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='tab-link'])[1]//a[2]")));
        element.click();

        WebElement element1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//button[@class='ant-btn css-ru2fok ant-btn-primary'])[1]")));
        element1.click();

        driver.findElement(By.xpath("(//input[@id='userName'])")).sendKeys(userName);
        WebElement user1RoleId = driver.findElement(By.xpath("//input[@id='roleId']"));
        user1RoleId.click();
        try {
            WebElement desiredOption = null;
            do {
                Actions actions = new Actions(driver);
                actions.sendKeys(Keys.ARROW_DOWN).build().perform();
                desiredOption = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'" + role + "')]")));
            } while (!desiredOption.isDisplayed());
            desiredOption.click();
        } catch (Exception e) {
            e.printStackTrace();
        }

        driver.findElement(By.xpath("//input[@id='email']")).sendKeys(email);
        driver.findElement(By.xpath(" //input[@id='contactNumber']")).sendKeys(mobileNum);
        driver.findElement(By.xpath("//input[@id='password']")).sendKeys(password);
        WebElement user1Site = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ahid']")));
        user1Site.click();
        WebElement user1BFALele = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='ant-select-tree-title']")));
        user1BFALele.click();
        driver.findElement(By.xpath("(//button[@type='submit'])[1]")).click();
        Boolean isValidUser = userValidation(userName);
        Assert.assertTrue(isValidUser);
    }

    @Test(priority = 3)
    public void testSuperUserAccess() throws IOException {
        driver.findElement(By.xpath("//a[normalize-space()='UserAccess']")).click();
        WebElement element = driver.findElement(By.xpath("//input[@id='roleName']"));
        ArrayList<String> roleData = getTestData("User Access");
        String superUser = roleData.get(1);
        this.providingUserAccess(superUser);
    }


    @Test(priority = 4)
    public void testLogout() throws IOException {
        ArrayList<String> userCredentials = getTestData("Login Admin");
        String username = userCredentials.get(1);
        driver.findElement(By.xpath("//strong[normalize-space()='" + username + "']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='Logout']")));
        logoutElement.click();
    }

    @Test(priority = 5)
    public void testSuccessfulLogin() throws IOException {

        ArrayList<String> userCredentials = getTestData("Login Super user");
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


    @Test(priority = 6)
    public void testManagerRoleCreation() throws IOException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        ArrayList<String> roleData = getTestData("Role Creation");
        String manager = roleData.get(2);
        try{
            Thread.sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }

        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[2]")));
        element.click();
        WebElement element1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='ant-tabs-nav-list'])[1]//div[1]//div//a")));
        element1.click();
        WebElement element2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='ant-row ant-row-space-between css-ru2fok'])//div[2]")));
        element2.click();
        WebElement roleNameInput2 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='roleName']")));
        roleNameInput2.sendKeys(manager);

        WebElement button2 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
        button2.click();

        Boolean isManagerRole2Added = validateRole(manager);
        Assert.assertTrue(isManagerRole2Added, "Expected text 'Test User' not found in any element");

    }

    public Boolean validateRole(String role){
        boolean hasNextPage = true;
        boolean foundText = false;

        while (hasNextPage) {
            List<WebElement> elementList = driver.findElements(By.xpath("//tr[@data-testId='row']//td[2]"));
            Iterator<WebElement> iterator = elementList.iterator();

            while (iterator.hasNext()) {
                WebElement webElement = iterator.next();

                if(webElement.getText().trim().equalsIgnoreCase(role)){
                    foundText = true;
                    break;
                }
            }

            try {
                WebElement nextPageButton = driver.findElement(By.xpath("(//button[@class='ant-pagination-item-link'])[2]"));
                if (nextPageButton.isEnabled()) {
                    nextPageButton.click();

                } else {
                    hasNextPage = false;
                }
            } catch (NoSuchElementException e) {
                System.out.println("No next page button found. Exiting loop.");
                hasNextPage = false;
            }
        }
        return foundText;
    }

    @Test(priority = 7)
    public void testTechnicianRoleCreation() throws IOException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        ArrayList<String> roleData = getTestData("Role Creation");
        String technician = roleData.get(3);
        try{
            Thread.sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }

        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[2]")));
        element.click();
        WebElement element1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='ant-tabs-nav-list'])[1]//div[1]//div//a")));
        element1.click();
        WebElement element2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='ant-row ant-row-space-between css-ru2fok'])//div[2]")));
        element2.click();
        WebElement roleNameInput2 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='roleName']")));
        roleNameInput2.sendKeys(technician);

        WebElement button2 = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
        button2.click();

        Boolean isManagerRole2Added = validateRole(technician);
        Assert.assertTrue(isManagerRole2Added, "Expected text 'Test User' not found in any element");

    }

    @DataProvider(name = "UserGroupData")
    public Object[][] getUserGroupData() throws IOException {
        FileInputStream fis = new FileInputStream("C:\\Users\\vinay\\Downloads\\TestData.xlsx");
        XSSFWorkbook workbook= new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(2);
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
    //span[normalize-space()='Save']
    @Test(priority = 8, dataProvider = "combinedUserData")
    public void testManagerAndTechUserCreation(String userName, String role, String email, String mobileNum, String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[@class='tab-link'])[1]//a[2]")));
        element.click();
        WebElement element1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//button[@class='ant-btn css-ru2fok ant-btn-primary'])[1]")));
        element1.click();

        driver.findElement(By.xpath("(//input[@id='userName'])")).sendKeys(userName);
        WebElement user1RoleId = driver.findElement(By.xpath("//input[@id='roleId']"));
        user1RoleId.click();
        try {
            WebElement desiredOption = null;
            do {
                Actions actions = new Actions(driver);
                actions.sendKeys(Keys.ARROW_DOWN).build().perform();
                desiredOption = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(text(),'" + role + "')]")));
            } while (!desiredOption.isDisplayed());
            desiredOption.click();
        } catch (Exception e) {
            e.printStackTrace();
        }

        driver.findElement(By.xpath("//input[@id='email']")).sendKeys(email);
        driver.findElement(By.xpath(" //input[@id='contactNumber']")).sendKeys(mobileNum);
        driver.findElement(By.xpath("//input[@id='password']")).sendKeys(password);
        WebElement user1Site = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ahid']")));
        user1Site.click();
        WebElement user1BFALele = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='ant-select-tree-title']")));
        user1BFALele.click();
        driver.findElement(By.xpath("(//button[@type='submit'])[1]")).click();
        Boolean isValidUser = userValidation(userName);
        Assert.assertTrue(isValidUser);
    }

    public Boolean userValidation(String userName){
        boolean hasNextPage = true;
        boolean foundText = false;

        while (hasNextPage) {
            List<WebElement> elementList = driver.findElements(By.xpath("//tr[@data-testId='row']//td[2]"));
            Iterator<WebElement> iterator = elementList.iterator();

            while (iterator.hasNext()) {
                WebElement webElement = iterator.next();
                System.out.println(webElement.getText() +" "+ userName);
                if(webElement.getText().trim().equalsIgnoreCase(userName)){
                    foundText = true;
                    break;
                }
            }
            try {
                WebElement nextPageButton = driver.findElement(By.xpath("//li[@title='Next Page']//button[@type='button']"));
                if (nextPageButton.isEnabled()) {
                    nextPageButton.click();
                } else {
                    hasNextPage = false;
                }
            } catch (NoSuchElementException e) {
                System.out.println("No next page button found. Exiting loop.");
                hasNextPage = false; // Exit loop if there's no next page button
            }
            if(foundText){
                break;
            }
        }

        System.out.println(foundText);

        List<WebElement> currentPage = driver.findElements(By.xpath("//a[@rel='nofollow']"));
        if(currentPage.size()>1){
            currentPage.get(0).click();
        }

        return foundText;
    }

    @Test(priority = 9, dataProvider = "UserGroupData")
    public void testUserGroupCreation(String groupName, String description, String user1, String user2){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        WebElement userGroupCreation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='User Group']")));
        userGroupCreation.click();
        WebElement addButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[normalize-space()='Add']")));
        addButton.click();

        driver.findElement(By.xpath("//input[@id='userGroupName']")).sendKeys(groupName);
        driver.findElement(By.xpath("//input[@id='description']")).sendKeys(description);
        WebElement site = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='ahid']")));
        site.click();
        WebElement BFALele = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[@class='ant-select-tree-title']")));
        BFALele.click();

        WebElement dropdown =driver.findElement(By.xpath("//input[@id='userIds']"));
        dropdown.click();

        dropdown.sendKeys(user1);
        WebElement member1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'"+user1+"')]")));
        member1.click();

        dropdown.sendKeys(user2);
        WebElement member2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(text(),'"+user2+"')]")));
        member2.click();
        ((JavascriptExecutor) driver).executeScript("arguments[0].style.display='none';", dropdown);

//        WebElement radioButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//input[@type='radio'])[1]")));
//        radioButton.click();
        WebElement saveButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[@type='submit']")));
        saveButton.click();


        boolean hasNextPage = true;
        boolean foundText = false;

        while (hasNextPage) {
            // Iterate through elements on the current page
            List<WebElement> elementList = driver.findElements(By.xpath("//tr[@data-testid='row']//td[2]"));
            Iterator<WebElement> iterator = elementList.iterator();

            while (iterator.hasNext()) {
                WebElement webElement = iterator.next();

                if(webElement.getText().trim().equalsIgnoreCase(groupName)){
                    foundText = true;
                    break;  // Exit the loop once the text is found
                }
            }

            // Check if there is a next page by verifying the presence of the next page button
            try {
                WebElement nextPageButton = driver.findElement(By.xpath("//li[@title='Next Page']//button"));
                if (nextPageButton.isEnabled()) {
                    nextPageButton.click(); // Click the next page button

                } else {
                    hasNextPage = false; // Set hasNextPage to false if there's no next page
                }
            } catch (NoSuchElementException e) {
                System.out.println("No next page button found. Exiting loop.");
                hasNextPage = false; // Exit loop if there's no next page button
            }
        }
        Assert.assertTrue(foundText, "Expected text 'Test Group' not found in any element");
    }

    @Test(priority = 10)
    public void testManagerUserAccess() throws IOException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//ul[@role='menu']//li[2]")));
        element.click();
        WebElement element1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='UserAccess']")));
        element1.click();
        ArrayList<String> roleData = getTestData("User Access");
        String manager = roleData.get(2);
        this.providingUserAccess(manager);
        try {
            Thread.sleep(3000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test(priority = 11)
    public void testTechnicianUserAccess() throws IOException {
        ArrayList<String> roleData = getTestData("User Access");
        String technician = roleData.get(3);
        this.providingUserAccess(technician);
    }

    public void providingUserAccess(String role) {
        String xpathExpression = "//div[contains(text(),'" + role + "')]";
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));

        WebElement roleNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@id='roleName']")));
        Actions actions = new Actions(driver);
        actions.moveToElement(roleNameInput).click().perform();
        roleNameInput.sendKeys(role);
        WebElement element1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathExpression)));
        element1.click();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (role.equalsIgnoreCase("Test SuperUser Role")) {
            for (int i = 1; i <= 35; i+=2) {
                driver.findElement(By.xpath("(//div[@class='ant-col css-ru2fok'])[" + i + "]")).click();
            }
            driver.findElement(By.xpath("//span[contains(text(),'Save')]")).click();
            wait.until(webDriver -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            });
        }
        else if(role.equalsIgnoreCase("Test Manager Role")){
            int index = 1;
            for (int i = 1; i <= 33; i+=2) {
                WebElement accessInputEle = driver.findElement(By.xpath("(//div[@class='ant-col css-ru2fok'])[" + i + "]"));
                WebElement accessTitleEle = null;
                accessTitleEle =  driver.findElement(By.xpath("(//article[@class='ant-typography css-ru2fok'])[" + index + "]"));
                index++;
                if (accessTitleEle.getText().equalsIgnoreCase("SMS/Mail Configuration")) {
                    System.out.println(accessTitleEle.getText());
                    WebElement label = driver.findElement(By.xpath("(//td[@class='ant-table-cell'])[30]//div//label[2]"));
                    label.click();
                    System.out.println(label.getText());
                    continue;
                }
                accessInputEle.click();
            }
            driver.findElement(By.xpath("//span[contains(text(),'Save')]")).click();
            wait.until(webDriver -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            });


        }
        else {
            for (int j = 1; j < 18; j++) {
                WebElement checkBox = driver.findElement(By.xpath("(//input[@value='View'])[" + j + "]"));
                if(j==6){
                    driver.findElement(By.xpath("(//div[@class='ant-col css-ru2fok'])[11]")).click();
                    continue;
                } else if (j==7) {
                    driver.findElement(By.xpath("(//div[@class='ant-col css-ru2fok'])[13]")).click();
                    continue;
                }
                checkBox.click();
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        driver.findElement(By.xpath("//span[contains(text(),'Save')]")).click();
        wait.until(webDriver -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        });
    }

    @Test(priority = 12)
    public void testSuperUserLogout() throws IOException {
        ArrayList<String> userCredentials = getTestData("Login Super User");
        String username = userCredentials.get(1);
        driver.findElement(By.xpath("//strong[normalize-space()='" + username + "']")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement logoutElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[normalize-space()='Logout']")));
        logoutElement.click();
    }
}


