package com.example.EcoBazaar_module2.service;

import com.example.EcoBazaar_module2.model.Product;
import com.example.EcoBazaar_module2.model.ProductCarbonData;
import com.example.EcoBazaar_module2.model.User;
import com.example.EcoBazaar_module2.model.Role;
import com.example.EcoBazaar_module2.repository.ProductCarbonDataRepository;
import com.example.EcoBazaar_module2.repository.ProductRepository;
import com.example.EcoBazaar_module2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCarbonDataRepository carbonDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ImageService imageService;

    public Page<Product> searchProducts(String name, String category, Double minPrice,
                                        Double maxPrice, Double maxCarbon, String sortBy,
                                        int page, int size, Boolean featured) {

        // Create pageable with sorting
        Sort sort = Sort.unsorted();
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc": sort = Sort.by("price").ascending(); break;
                case "price_desc": sort = Sort.by("price").descending(); break;
                case "rating": sort = Sort.by("averageRating").descending(); break;
                case "popular": sort = Sort.by("soldCount").descending(); break;
                case "newest": sort = Sort.by("createdAt").descending(); break;
                case "carbon_asc": sort = Sort.by("carbonData.manufacturing").ascending(); break;
                default: sort = Sort.by("createdAt").descending();
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        // Use repository method with specifications
        Page<Product> products = productRepository.searchProducts(
                name,
                (category != null && !category.equals("All")) ? category : null,
                minPrice,
                maxPrice,
                featured,
                pageable
        );

        // Filter by carbon if specified (done in memory for simplicity)
        if (maxCarbon != null) {
            List<Product> filtered = products.getContent().stream()
                    .filter(p -> p.getTotalCarbonFootprint() <= maxCarbon)
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered, pageable, filtered.size());
        }

        return products;
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public void incrementProductView(Long productId) {
        Product product = getProductById(productId);
        product.incrementViewCount();
        productRepository.save(product);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndVerifiedTrueAndActiveTrue();
    }

    public List<Product> getTrendingProducts() {
        Pageable topTen = PageRequest.of(0, 10, Sort.by("soldCount").descending());
        return productRepository.findByVerifiedTrueAndActiveTrue(topTen);
    }

    @Transactional
    public Product createProduct(Long sellerId, String name, String description, Double price,
                                 Integer quantity, String category, String images,
                                 ProductCarbonData carbonData) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (seller.getRole() != Role.SELLER && seller.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized: Only Sellers or Admins can add products");
        }

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCategory(category);
        product.setImages(images);
        product.setSeller(seller);
        product.setActive(true);
        product.setVerified(seller.getRole() == Role.ADMIN);

        Product savedProduct = productRepository.save(product);

        if (isCarbonDataEmpty(carbonData)) {
            calculateAutomaticCarbon(carbonData, category, price);
        }

        carbonData.setProduct(savedProduct);
        carbonDataRepository.save(carbonData);

        auditService.log(sellerId, "CREATE_PRODUCT", "PRODUCT", savedProduct.getId(),
                "Product: " + name);

        return savedProduct;
    }

    @Transactional
    public List<String> addProductImages(Long productId, MultipartFile[] files) {
        Product product = getProductById(productId);
        List<String> imageNames = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String fileName = imageService.saveProductImage(file, productId);
                imageNames.add(fileName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        // Append to existing images
        String existingImages = product.getImages();
        String newImages = String.join(",", imageNames);

        if (existingImages != null && !existingImages.isEmpty()) {
            product.setImages(existingImages + "," + newImages);
        } else {
            product.setImages(newImages);
        }

        productRepository.save(product);
        return imageNames;
    }

    @Transactional
    public Product updateProduct(Long userId, Long productId, String name, String description,
                                 Double price, Integer quantity, String category, String images,
                                 ProductCarbonData newCarbonData) {
        Product product = getProductById(productId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!product.getSeller().getId().equals(userId) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized");
        }

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setCategory(category);

        if (images != null && !images.isEmpty()) {
            product.setImages(images);
        }

        if (newCarbonData != null) {
            if (user.getRole() == Role.SELLER) {
                product.setVerified(false);
            }

            ProductCarbonData existingData = product.getCarbonData();
            if (isCarbonDataEmpty(newCarbonData)) {
                calculateAutomaticCarbon(existingData, category, price);
            } else {
                existingData.setManufacturing(newCarbonData.getManufacturing());
                existingData.setTransportation(newCarbonData.getTransportation());
                existingData.setPackaging(newCarbonData.getPackaging());
                existingData.setUsage(newCarbonData.getUsage());
                existingData.setDisposal(newCarbonData.getDisposal());
            }
            carbonDataRepository.save(existingData);
        }

        Product updated = productRepository.save(product);
        auditService.log(userId, "UPDATE_PRODUCT", "PRODUCT", productId, null);

        return updated;
    }

    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Product product = getProductById(productId);

        if (user.getRole() != Role.ADMIN && !product.getSeller().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        productRepository.delete(product);
        auditService.log(userId, "DELETE_PRODUCT", "PRODUCT", productId, "Deleted");
    }

    @Transactional
    public void toggleFeatured(Long adminId, Long productId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Unauthorized: Admin only");
        }

        Product product = getProductById(productId);
        product.setFeatured(!product.isFeatured());
        productRepository.save(product);

        auditService.log(adminId, "TOGGLE_FEATURED", "PRODUCT", productId,
                "Featured: " + product.isFeatured());
    }

    public List<Product> getSellerProducts(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public List<Product> getPendingProducts() {
        return productRepository.findByVerifiedFalse();
    }

    @Transactional
    public void verifyProduct(Long adminId, Long productId) {
        Product product = getProductById(productId);
        product.setVerified(true);
        productRepository.save(product);
        auditService.log(adminId, "VERIFY_PRODUCT", "PRODUCT", productId, null);
    }

    private boolean isCarbonDataEmpty(ProductCarbonData data) {
        return data.getManufacturing() == 0 && data.getTransportation() == 0 &&
                data.getPackaging() == 0 && data.getUsage() == 0 && data.getDisposal() == 0;
    }

    private void calculateAutomaticCarbon(ProductCarbonData data, String category, Double price) {
        double base = 5.0;
        if (category != null) {
            switch (category.toLowerCase()) {
                case "electronics": base = 50.0; break;
                case "fashion":
                case "clothing": base = 12.0; break;
                case "home":
                case "furniture": base = 25.0; break;
                case "food": base = 3.0; break;
                case "beauty": base = 2.0; break;
                default: base = 5.0;
            }
        }
        data.setManufacturing(base * 0.6);
        data.setTransportation(base * 0.2);
        data.setPackaging(base * 0.1);
        data.setDisposal(base * 0.1);
        data.setUsage(0.0);
    }
}