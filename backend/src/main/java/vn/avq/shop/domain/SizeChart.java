package vn.avq.shop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "size_charts")
public class SizeChart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chart_id") private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") private Product product;
    private String size;
    @Column(name = "height_min") private int heightMin;
    @Column(name = "height_max") private int heightMax;
    @Column(name = "weight_min") private int weightMin;
    @Column(name = "weight_max") private int weightMax;

    protected SizeChart() {}
    public SizeChart(Product product, String size, int hMin, int hMax, int wMin, int wMax) {
        this.product = product; this.size = size; heightMin = hMin; heightMax = hMax; weightMin = wMin; weightMax = wMax;
    }
    public boolean contains(int heightCm, int weightKg) {
        return heightCm >= heightMin && heightCm <= heightMax && weightKg >= weightMin && weightKg <= weightMax;
    }
    public String getSize() { return size; }
    public Product getProduct() { return product; }
    public int getHeightMin() { return heightMin; }
    public int getHeightMax() { return heightMax; }
    public int getWeightMin() { return weightMin; }
    public int getWeightMax() { return weightMax; }
}
