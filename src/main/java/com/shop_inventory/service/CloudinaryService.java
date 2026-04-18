package com.shop_inventory.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Transformation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.folder}")
    private String folder;

    private static final List<String> ALLOWED_TYPES =
            Arrays.asList("image/jpeg", "image/jpg",
                    "image/png", "image/webp");

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload image to Cloudinary.
     * Returns the secure HTTPS URL of the uploaded image.
     */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file, Long itemId) throws IOException {

        // Validate type
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Only JPG, PNG and WEBP images are allowed");
        }

        // Validate size — 5MB max
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Image size must be under 5MB");
        }

        // Upload options
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder,
                "public_id", "item_" + itemId,
                "overwrite", true,
                "resource_type", "image",
                "transformation", new Transformation()
                        .width(800)
                        .height(800)
                        .crop("limit")
                        .quality("auto")
                        .fetchFormat("auto")
        );

        Map<String, Object> result =
                cloudinary.uploader().upload(file.getBytes(), options);

        String url = (String) result.get("secure_url");
        System.out.println("✅ Uploaded to Cloudinary: " + url);
        return url;
    }

    /**
     * Delete image from Cloudinary by its public_id.
     * public_id format: hardware-shop/items/item_42
     */
    @SuppressWarnings("unchecked")
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            Map<String, Object> result =
                    cloudinary.uploader().destroy(publicId,
                            ObjectUtils.asMap("resource_type", "image"));
            System.out.println("🗑 Deleted from Cloudinary: "
                    + publicId + " → " + result.get("result"));
        } catch (IOException e) {
            System.err.println("Could not delete from Cloudinary: "
                    + e.getMessage());
        }
    }

    /**
     * Build Cloudinary public_id from folder + itemId.
     * Used to delete the image when item is deleted.
     */
    public String buildPublicId(Long itemId) {
        return folder + "/item_" + itemId;
    }
}
