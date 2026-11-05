package vn.avq.shop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id") private Long id;
    private String name;
    private String slug;
    protected Category() {}
    public Category(String name, String slug) { this.name = name; this.slug = slug; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
}
