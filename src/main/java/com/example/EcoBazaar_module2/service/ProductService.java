package com.example.EcoBazaar_module2.service;

import com.example.EcoBazaar_module2.model.Product;
import com.example.EcoBazaar_module2.model.ProductCarbonData;
import com.example.EcoBazaar_module2.model.User;
import com.example.EcoBazaar_module2.repository.ProductCarbonDataRepository;
import com.example.EcoBazaar_module2.repository.ProductRepository;
import com.example.EcoBazaar_module2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<Product> getAllVerifiedProducts() {
        return productRepository.findByVerifiedTrue();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public Product createProduct(Long sellerId, String name, String description, Double price,
                                 String category, String imageUrl, ProductCarbonData carbonData) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setSeller(seller);
        product.setActive(true);
        product.setVerified(false);

        Product savedProduct = productRepository.save(product);

        carbonData.setProduct(savedProduct);
        carbonDataRepository.save(carbonData);

        auditService.log(sellerId, "CREATE_PRODUCT", "PRODUCT", savedProduct.getId(),
                "Product: " + name);

        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Long sellerId, Long productId, String name, String description,
                                 Double price, String category, String imageUrl, ProductCarbonData newCarbonData) {
        Product product = getProductById(productId);

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Unauthorized: Not product owner");
        }

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setImageUrl(imageUrl);

        if (newCarbonData != null) {
            product.setVerified(false); // Reset verification if carbon data changes
            ProductCarbonData existingData = product.getCarbonData();
            existingData.setManufacturing(newCarbonData.getManufacturing());
            existingData.setTransportation(newCarbonData.getTransportation());
            existingData.setPackaging(newCarbonData.getPackaging());
            existingData.setUsage(newCarbonData.getUsage());
            existingData.setDisposal(newCarbonData.getDisposal());
            carbonDataRepository.save(existingData);
        }

        Product updated = productRepository.save(product);
        auditService.log(sellerId, "UPDATE_PRODUCT", "PRODUCT", productId, null);

        return updated;
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

    @Transactional
    public void deactivateProduct(Long actorId, Long productId, String reason) {
        Product product = getProductById(productId);
        product.setActive(false);
        productRepository.save(product);

        auditService.log(actorId, "DEACTIVATE_PRODUCT", "PRODUCT", productId, reason);
    }
}