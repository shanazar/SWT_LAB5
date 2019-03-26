import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminTest extends TestHelper {

    public static final Random RANDOM = new Random();
    public static final DecimalFormat EURO_FORMAT = new DecimalFormat("#.00");

    private String username;
    private String password;
    private String product_title;
    private String product_type;

    private boolean requiresUserCleanup;
    private boolean requiresProductCleanup;

    @Test
    public void titleExistsTest() {
        String expectedTitle = "ST Online Store";
        String actualTitle = driver.getTitle();

        assertEquals(expectedTitle, actualTitle);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        requiresUserCleanup = false;
        requiresProductCleanup = false;
        username = UUID.randomUUID().toString();
        password = UUID.randomUUID().toString();
    }

    @Override
    @After
    public void tearDown() {
        while (true) {
            try {
                if ((requiresUserCleanup || requiresProductCleanup) && !driver.findElement(By.xpath("//*[@id='menu']//li[3]/a")).getText().equals("Logout")) {
                    login(username, password);
                }
                break;
            } catch (StaleElementReferenceException e) {
                System.out.println("Failed to find menu buttons. Trying again.");
            }
        }
        if (requiresProductCleanup) {
            if (!driver.getCurrentUrl().endsWith("products"))
                driver.get(baseUrl + "products");
            driver.findElement(By.xpath("//*[@id='" + product_title + "']//a[text()='Delete']")).click();
        }
        if (requiresUserCleanup) {
            if (!driver.getCurrentUrl().equals(baseUrlAdmin))
                driver.get(baseUrlAdmin);
            driver.findElement(By.linkText("Delete")).click();
        }
        super.tearDown();
    }

    // register an account
    @Test
    public void registerTest() {
        // Arrange
        requiresUserCleanup = true;
        driver.get(baseUrlAdmin);
        driver.findElement(By.linkText("Register")).click();

        // Act
        driver.findElement(By.id("user_name")).sendKeys(username);
        driver.findElement(By.id("user_password")).sendKeys(password);
        driver.findElement(By.id("user_password_confirmation")).sendKeys(password);
        driver.findElement(By.xpath("//input[@value='Create User']")).click();

        // Assert
        assertEquals("User " + username + " was successfully created.", driver.findElement(By.id("notice")).getText());
    }

    // register an account improperly
    @Test
    public void negativeRegisterTest() {
        // Arrange
        requiresUserCleanup = true;
        driver.get(baseUrlAdmin);
        driver.findElement(By.linkText("Register")).click();
        String passwordConfirmation = UUID.randomUUID().toString();

        // Act
        driver.findElement(By.id("user_name")).sendKeys(username);
        driver.findElement(By.id("user_password")).sendKeys(password);
        driver.findElement(By.id("user_password_confirmation")).sendKeys(passwordConfirmation);
        driver.findElement(By.xpath("//input[@value='Create User']")).click();

        // Assert
        assertEquals("Password confirmation doesn't match Password", driver.findElement(By.xpath("//*[@id='error_explanation']//li")).getText());
        requiresUserCleanup = false; // Assuming action failed and no user was actually created.
    }

    // login to the system
    @Test
    public void loginTest() {
        logoutTest(); // registers an account as a dependency
        login(username, password);

        assertTrue(driver.findElement(By.linkText("Logout")).isDisplayed());
    }

    // login to the system with no existing account
    @Test
    public void negativeLoginTestNoAccount() {
        login(username, password);

        assertEquals("Invalid user/password combination", driver.findElement(By.id("notice")).getText());
    }

    // logout from the system
    @Test
    public void logoutTest() {
        registerTest();
        driver.findElement(By.linkText("Logout")).click();

        assertTrue(driver.findElement(By.linkText("Login")).isDisplayed());

    }

    // delete an account
    @Test
    public void deleteAccountTest() {
        registerTest();

        driver.get(baseUrlAdmin);
        driver.findElement(By.linkText("Delete")).click();

        negativeLoginTestNoAccount(); // This test should pass, because user should no longer exist
        requiresUserCleanup = false; // Test passed, so cleanup done.
    }

    // add products
    @Test
    public void addProductTest() {
        registerTest();
        requiresProductCleanup = true;
        product_title = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();
        String price = EURO_FORMAT.format(RANDOM.nextInt(5000) / 100.0);

        driver.findElement(By.linkText("New product")).click();
        driver.findElement(By.id("product_title")).sendKeys(product_title);
        driver.findElement(By.id("product_description")).sendKeys(description);
        Select typeSelector = new Select(driver.findElement(By.id("product_prod_type")));
        List<String> types = typeSelector.getOptions().stream().skip(1).map(WebElement::getText).collect(Collectors.toList());
        product_type = types.get(RANDOM.nextInt(types.size()));
        typeSelector.selectByValue(product_type);
        driver.findElement(By.id("product_price")).sendKeys(price);
        driver.findElement(By.xpath("//input[@value='Create Product']")).click();

        driver.findElement(By.id(product_title)).findElement(By.tagName("a")).click();
        assertEquals("Title: " + product_title, driver.findElement(By.xpath("//strong[text()='Title:']/..")).getText());
        assertEquals("Description: " + description, driver.findElement(By.xpath("//strong[text()='Description:']/..")).getText());
        assertEquals("Type: " + product_type, driver.findElement(By.xpath("//strong[text()='Type:']/..")).getText());
        assertEquals("Price: €" + price, driver.findElement(By.xpath("//strong[text()='Price:']/..")).getText());
    }

    // add products improperly
    @Test
    public void negativeAddProductTestBadPrice() {
        registerTest();
        requiresProductCleanup = true;
        product_title = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();
        String price = UUID.randomUUID().toString();

        driver.findElement(By.linkText("New product")).click();
        driver.findElement(By.id("product_title")).sendKeys(product_title);
        driver.findElement(By.id("product_description")).sendKeys(description);
        Select typeSelector = new Select(driver.findElement(By.id("product_prod_type")));
        List<String> types = typeSelector.getOptions().stream().skip(1).map(WebElement::getText).collect(Collectors.toList());
        product_type = types.get(RANDOM.nextInt(types.size()));
        typeSelector.selectByValue(product_type);
        driver.findElement(By.id("product_price")).sendKeys(price);
        driver.findElement(By.xpath("//input[@value='Create Product']")).click();

        assertEquals("Price is not a number", driver.findElement(By.xpath("//*[@id='error_explanation']//li")).getText());
        requiresProductCleanup = false; // Assuming action failed and no user was actually created.
    }

    // edit products
    @Test
    public void editProductTest() {
        addProductTest();

        driver.findElement(By.linkText("Edit")).click();

        product_title = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();
        String price = EURO_FORMAT.format(RANDOM.nextInt(5000) / 100.0);

        driver.findElement(By.id("product_title")).clear();
        driver.findElement(By.id("product_title")).sendKeys(product_title);
        driver.findElement(By.id("product_description")).clear();
        driver.findElement(By.id("product_description")).sendKeys(description);
        Select typeSelector = new Select(driver.findElement(By.id("product_prod_type")));
        List<String> types = typeSelector.getOptions().stream().skip(1).map(WebElement::getText).filter(a -> !a.equals(product_type)).collect(Collectors.toList());
        product_type = types.get(RANDOM.nextInt(types.size()));
        typeSelector.selectByValue(product_type);
        driver.findElement(By.id("product_price")).clear();
        driver.findElement(By.id("product_price")).sendKeys(price);
        driver.findElement(By.xpath("//input[@value='Update Product']")).click();

        assertEquals("Title: " + product_title, driver.findElement(By.xpath("//strong[text()='Title:']/..")).getText());
        assertEquals("Description: " + description, driver.findElement(By.xpath("//strong[text()='Description:']/..")).getText());
        assertEquals("Type: " + product_type, driver.findElement(By.xpath("//strong[text()='Type:']/..")).getText());
        assertEquals("Price: €" + price, driver.findElement(By.xpath("//strong[text()='Price:']/..")).getText());
    }

    // edit products improperly
    @Test
    public void negativeEditProductTestBadPrice() {
        addProductTest();

        driver.findElement(By.linkText("Edit")).click();

        String prev_product_title = product_title;
        String prev_product_type = product_type;
        product_title = UUID.randomUUID().toString();
        String description = UUID.randomUUID().toString();
        String price = UUID.randomUUID().toString();

        driver.findElement(By.id("product_title")).clear();
        driver.findElement(By.id("product_title")).sendKeys(product_title);
        driver.findElement(By.id("product_description")).clear();
        driver.findElement(By.id("product_description")).sendKeys(description);
        Select typeSelector = new Select(driver.findElement(By.id("product_prod_type")));
        List<String> types = typeSelector.getOptions().stream().skip(1).map(WebElement::getText).filter(a -> !a.equals(product_type)).collect(Collectors.toList());
        product_type = types.get(RANDOM.nextInt(types.size()));
        typeSelector.selectByValue(product_type);
        driver.findElement(By.id("product_price")).clear();
        driver.findElement(By.id("product_price")).sendKeys(price);
        driver.findElement(By.xpath("//input[@value='Update Product']")).click();

        assertEquals("Price is not a number", driver.findElement(By.xpath("//*[@id='error_explanation']//li")).getText());
        product_title = prev_product_title;
        product_type = prev_product_type;
    }

    // delete products
    @Test
    public void deleteProductTest() {
        addProductTest();
        driver.findElement(By.linkText("Back")).click();

        driver.findElement(By.xpath("//*[@id='" + product_title + "']//a[text()='Delete']")).click();

        assertFalse(isElementPresent(By.id(product_title)));
        requiresProductCleanup = false; // Test passed means product cleanup not necessary.
    }

}
