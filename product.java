package database_setup;

public class Product {
    private int productId;
    private String productName;
    private String category;
    private double price;
    private int stock;
    private String description;
    private String imageUrl;

    public Product() {}

    public Product(int productId, String productName, String category,
                   double price, int stock, String description) {
        this.productId   = productId;
        this.productName = productName;
        this.category    = category;
        this.price       = price;
        this.stock       = stock;
        this.description = description;
    }

    public int    getProductId()    { return productId;    }
    public String getProductName()  { return productName;  }
    public String getCategory()     { return category;     }
    public double getPrice()        { return price;        }
    public int    getStock()        { return stock;        }
    public String getDescription()  { return description;  }
    public String getImageUrl()     { return imageUrl;     }

    public void setProductId(int v)     { productId    = v; }
    public void setProductName(String v){ productName  = v; }
    public void setCategory(String v)   { category     = v; }
    public void setPrice(double v)      { price        = v; }
    public void setStock(int v)         { stock        = v; }
    public void setDescription(String v){ description  = v; }
    public void setImageUrl(String v)   { imageUrl     = v; }

    @Override
    public String toString() { return productName; }
}   // ← this closing brace was missing
