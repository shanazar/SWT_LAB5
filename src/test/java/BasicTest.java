import junit.framework.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;

public class BasicTest extends TestHelper {


    private String username = "";
    private String password = "";

    //Test if items do get added to shopping cart
    @Test
    public void addAllItemsToShoppingCart() throws InterruptedException {

        List<WebElement> entries = getEntries();

        assertEquals(0, getCartEntries().size());

        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(2);
        List<WebElement> cartEntries = getCartEntries();
        assertEquals(entries.size(), cartEntries.size());
    }

    //Test if the items added to the shopping cart match in names and in order
    @Test
    public void checkShoppingCartNamesMatchCatalogue() throws InterruptedException {
        List<WebElement> entries = getEntries();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(2);
        ArrayList<String> names = new ArrayList<String>();
        List<WebElement> cartEntries = getCartEntries();
        for (int i = 0; i < cartEntries.size(); i++) {
            String cartEntry = getShoppingCartItemTitle(cartEntries.get(i));
            names.add(cartEntry);

        }
        for (int i = 0; i < entries.size(); i++) {
            String shopEntry = getItemTitle(entries.get(i));
            if (!names.contains(shopEntry))
                assertEquals("Cart doesn't contain the item:", shopEntry, false);
        }
    }

    @Test
    public void checkShoppingCartPricesMatchCatalogue() throws InterruptedException {
        List<WebElement> entries = getEntries();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(1);
        List<WebElement> cartEntries = getCartEntries();
        for (int i = 0; i < entries.size(); i++) {
            String shopEntry = getItemPrice(entries.get(i));
            String cartEntry = getShoppingCartItemPrice(cartEntries.get(i));
            assertEquals("Prices from added items and shopping cart items were not identical.", shopEntry, cartEntry);
        }
    }

    @Test
    public void checkShoppingCartIncreaseAndDecrease() throws InterruptedException {
        Integer increaseAmount = 4;
        increaseQuantity(increaseAmount);
        increaseAmount++;
        String increaseAmountString = increaseAmount.toString() + "×";
        String realAmount = getShoppingCartAmount();
        assertEquals(increaseAmountString, realAmount);
        decreaseQuantity(increaseAmount);
        String originalAmount = "1" + "×";
        realAmount = getShoppingCartAmount();
        assertEquals(originalAmount, realAmount);
    }

    @Test
    public void deleteItemsOneByOne() throws InterruptedException {
        List<WebElement> entries = getEntries();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(1);
        assertEquals(getCartEntries().size(), entries.size());
        for (int i = 0; i < entries.size(); i++) {
            removeFirstItem();
        }
        assertEquals(0, getCartEntries().size());
    }

    @Test
    public void emptyCart() throws InterruptedException {
        List<WebElement> entries = getEntries();
        addItemsToShoppingCart(entries);
        TimeUnit.SECONDS.sleep(1);
        assertEquals(getCartEntries().size(), entries.size());
        emptyShoppingCart();
        assertEquals(0, getCartEntries().size());
    }

    @Test
    public void categoryTesting() throws InterruptedException {

        HashMap<String, ArrayList<String>> hashMap = getCategories();
        switchToSunglassesTab();
        TimeUnit.SECONDS.sleep(1);

        ArrayList<String> sunglassTitles = hashMap.get("Sunglasses");
        ArrayList<String> realTitles = getTitles();
        assertEquals(sunglassTitles, realTitles);


        switchToBooksTab();
        TimeUnit.SECONDS.sleep(1);
        ArrayList<String> booksTitles = hashMap.get("Books");
        realTitles = getTitles();
        Collections.reverse(realTitles);
        assertEquals(booksTitles, realTitles);


        switchToOtherTab();
        TimeUnit.SECONDS.sleep(1);
        ArrayList<String> otherTitles = hashMap.get("Other");
        realTitles = getTitles();
        Collections.reverse(realTitles);
        assertEquals(otherTitles, realTitles);
    }

    @Test
    public void testSearching() throws InterruptedException {
        HashMap<String, String> titleSearches = titleSearches();
        ArrayList<String> titles = getTitles();
        WebElement searchBox = getSearchBox();
        for (String title : titles) {

            TimeUnit.SECONDS.sleep(1);

            searchBox.sendKeys(titleSearches.get(title));
            TimeUnit.SECONDS.sleep(3);

            ArrayList<String> results = getTitles();
            assertEquals("The results did not contain the nessecary result", true, results.contains(title));
            searchBox.clear();
        }
    }

    @Test
    public void testCheckout() throws InterruptedException {
        addItemsToShoppingCart(getEntries());
        increaseQuantity(2);
        clickCheckoutButton();

    }
}
