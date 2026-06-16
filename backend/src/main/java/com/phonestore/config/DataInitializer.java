package com.phonestore.config;

import com.phonestore.model.*;
import com.phonestore.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Autowired private PasswordEncoder encoder;

    @Bean
    CommandLineRunner initData(UserRepository userRepo,
                               ProductRepository productRepo,
                               CategoryRepository categoryRepo) {
        return args -> {
            if (userRepo.count() > 0) return;

            // Categories
            Category iphone = categoryRepo.save(Category.builder().name("iPhone").slug("iphone").icon("bi-apple").sortOrder(1).build());
            Category samsung = categoryRepo.save(Category.builder().name("Samsung").slug("samsung").icon("bi-phone").sortOrder(2).build());
            Category xiaomi = categoryRepo.save(Category.builder().name("Xiaomi").slug("xiaomi").icon("bi-phone-fill").sortOrder(3).build());
            Category oppo = categoryRepo.save(Category.builder().name("OPPO").slug("oppo").icon("bi-phone").sortOrder(4).build());

            // Admin user
            userRepo.save(User.builder()
                .username("admin")
                .email("admin@phonestore.vn")
                .password(encoder.encode("admin123"))
                .fullName("Quản trị viên")
                .phone("0900000000")
                .role(User.Role.ADMIN)
                .isActive(true)
                .build());

            // Regular user
            userRepo.save(User.builder()
                .username("user")
                .email("user@phonestore.vn")
                .password(encoder.encode("user123"))
                .fullName("Nguyễn Văn A")
                .phone("0901234567")
                .role(User.Role.USER)
                .isActive(true)
                .build());

            // Products
            productRepo.saveAll(List.of(
                Product.builder()
                    .name("iPhone 15 Pro Max 256GB")
                    .sku("IPH-15PM-256-BLK")
                    .brand("iPhone").category(iphone)
                    .price(BigDecimal.valueOf(34990000))
                    .oldPrice(BigDecimal.valueOf(37990000))
                    .stock(15).soldCount(234)
                    .ram("8GB").storage("256GB").color("Titan Đen")
                    .cpu("Apple A17 Pro").display("6.7\" Super Retina XDR")
                    .camera("48MP + 12MP + 12MP").battery("4422 mAh")
                    .os("iOS 17").charging("MagSafe 15W")
                    .description("iPhone 15 Pro Max với chip A17 Pro, camera 48MP Periscope Zoom 5x.")
                    .images(List.of("https://cdn2.cellphones.com.vn/x/media/catalog/product/i/p/iphone-15-pro-max.png"))
                    .isNew(true).isFeatured(true).avgRating(4.8).reviewCount(234)
                    .build(),

                Product.builder()
                    .name("Samsung Galaxy S24 Ultra 512GB")
                    .sku("SAM-S24U-512").brand("Samsung").category(samsung)
                    .price(BigDecimal.valueOf(31990000))
                    .oldPrice(BigDecimal.valueOf(35990000))
                    .stock(8).soldCount(189)
                    .ram("12GB").storage("512GB").color("Titan Đen")
                    .cpu("Snapdragon 8 Gen 3").display("6.8\" Dynamic AMOLED 2X")
                    .camera("200MP + 50MP + 12MP").battery("5000 mAh")
                    .os("Android 14").charging("45W")
                    .description("Samsung Galaxy S24 Ultra với bút S Pen tích hợp, camera 200MP.")
                    .images(List.of("https://cdn2.cellphones.com.vn/x/media/catalog/product/s/a/samsung-s24-ultra.png"))
                    .isFeatured(true).isHot(true).avgRating(4.7).reviewCount(189)
                    .build(),

                Product.builder()
                    .name("iPhone 15 128GB")
                    .sku("IPH-15-128").brand("iPhone").category(iphone)
                    .price(BigDecimal.valueOf(22490000))
                    .oldPrice(BigDecimal.valueOf(24990000))
                    .stock(20).soldCount(312)
                    .ram("6GB").storage("128GB").color("Hồng")
                    .cpu("Apple A16 Bionic").display("6.1\" Super Retina XDR")
                    .camera("48MP + 12MP").battery("3877 mAh")
                    .os("iOS 17").charging("MagSafe 15W")
                    .description("iPhone 15 với cổng USB-C, Dynamic Island, camera 48MP.")
                    .images(List.of("https://cdn2.cellphones.com.vn/x/media/catalog/product/i/p/iphone-15.png"))
                    .avgRating(4.6).reviewCount(312)
                    .build(),

                Product.builder()
                    .name("Xiaomi 14 Ultra 512GB")
                    .sku("XMI-14U-512").brand("Xiaomi").category(xiaomi)
                    .price(BigDecimal.valueOf(29990000))
                    .oldPrice(BigDecimal.valueOf(32990000))
                    .stock(5).soldCount(87)
                    .ram("16GB").storage("512GB").color("Đen")
                    .cpu("Snapdragon 8 Gen 3").display("6.73\" AMOLED 2K")
                    .camera("50MP Leica x4").battery("5000 mAh")
                    .os("Android 14").charging("90W")
                    .description("Xiaomi 14 Ultra hợp tác Leica, camera Summilux 1 inch.")
                    .images(List.of("https://cdn2.cellphones.com.vn/x/media/catalog/product/x/i/xiaomi-14-ultra.png"))
                    .isHot(true).avgRating(4.5).reviewCount(87)
                    .build(),

                Product.builder()
                    .name("Samsung Galaxy A55 5G 256GB")
                    .sku("SAM-A55-256").brand("Samsung").category(samsung)
                    .price(BigDecimal.valueOf(11490000))
                    .oldPrice(BigDecimal.valueOf(12990000))
                    .stock(30).soldCount(156)
                    .ram("8GB").storage("256GB").color("Xanh")
                    .cpu("Exynos 1480").display("6.6\" Super AMOLED FHD+")
                    .camera("50MP + 12MP + 5MP").battery("5000 mAh")
                    .os("Android 14").charging("25W")
                    .description("Samsung Galaxy A55 5G thiết kế cao cấp, camera 50MP OIS.")
                    .images(List.of("https://cdn2.cellphones.com.vn/x/media/catalog/product/s/a/samsung-a55.png"))
                    .avgRating(4.4).reviewCount(156)
                    .build(),

                Product.builder()
                    .name("OPPO Reno 11 Pro 256GB")
                    .sku("OPP-R11P-256").brand("OPPO").category(oppo)
                    .price(BigDecimal.valueOf(13990000))
                    .oldPrice(BigDecimal.valueOf(15990000))
                    .stock(12).soldCount(94)
                    .ram("12GB").storage("256GB").color("Xanh ngọc")
                    .cpu("MediaTek Dimensity 8200").display("6.7\" AMOLED 120Hz")
                    .camera("50MP + 8MP + 32MP").battery("4600 mAh")
                    .os("Android 14").charging("67W")
                    .description("OPPO Reno 11 Pro với camera chân dung đột phá, sạc nhanh 67W.")
                    .images(List.of("https://cdn2.cellphones.com.vn/x/media/catalog/product/o/p/oppo-reno11-pro.png"))
                    .avgRating(4.3).reviewCount(94)
                    .build(),

                Product.builder()
                    .name("Xiaomi Redmi Note 13 Pro 256GB")
                    .sku("XMI-RN13P-256").brand("Xiaomi").category(xiaomi)
                    .price(BigDecimal.valueOf(8490000))
                    .oldPrice(BigDecimal.valueOf(9990000))
                    .stock(25).soldCount(203)
                    .ram("8GB").storage("256GB").color("Đen")
                    .cpu("Snapdragon 7s Gen 2").display("6.67\" AMOLED 120Hz")
                    .camera("200MP + 8MP + 2MP").battery("5100 mAh")
                    .os("Android 13").charging("67W")
                    .description("Redmi Note 13 Pro với camera 200MP, pin 5100mAh, sạc 67W.")
                    .images(List.of("https://cdn2.cellphones.com.vn/x/media/catalog/product/r/e/redmi-note-13-pro.png"))
                    .isHot(true).avgRating(4.5).reviewCount(203)
                    .build()
            ));

            System.out.println("✅ Dữ liệu mẫu đã được tạo!");
            System.out.println("   Admin: admin / admin123");
            System.out.println("   User:  user / user123");
        };
    }
}
