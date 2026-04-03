package database_setup;

public class CartItem {
    private int     cartId;
    private int     userId;
    private Product product;
    private int     quantity;

    public CartItem(int cartId, int userId, Product product, int quantity) {
        this.cartId   = cartId;
        this.userId   = userId;
        this.product  = product;
        this.quantity = quantity;
    }

    public int     getCartId()        { return cartId;   }
    public int     getUserId()        { return userId;   }
    public Product getProduct()       { return product;  }
    public int     getQuantity()      { return quantity; }
    public void    setQuantity(int q) { quantity = q;    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}
