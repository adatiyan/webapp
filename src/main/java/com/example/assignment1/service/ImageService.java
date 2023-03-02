package com.example.assignment1.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;

import com.example.assignment1.exeception.DataNotFoundExeception;
import com.example.assignment1.exeception.BadInputException;
import com.example.assignment1.model.Image;
import com.example.assignment1.repository.ImageRepository;


@Service
public class ImageService {

	@Value("${aws.s3.bucketName}")
	private String bucketName;
	
	@Autowired
	private FileStore fileStore;

	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
    private AmazonS3 amazonS3;

	public Image saveImage(Long productId, Long userId, MultipartFile file) {
		// check if the file is empty
		if (file.isEmpty()) {
			throw new IllegalStateException("Cannot upload empty file");
		}
		System.out.println("30");
		// Check if the file is an image
//        if (!Arrays.asList(IMAGE_PNG.getMimeType(),
//                IMAGE_BMP.getMimeType(),
//                IMAGE_GIF.getMimeType(),
//                IMAGE_JPEG.getMimeType()).contains(file.getContentType())) {
//            throw new IllegalStateException("FIle uploaded is not an image");
//        }
		// get file metadata
		Map<String, String> metadata = new HashMap<>();
		metadata.put("Content-Type", file.getContentType());
		metadata.put("Content-Length", String.valueOf(file.getSize()));
		// Save Image in S3 and then save Todo in the database
		String path = String.format("%s/%s/%s", bucketName, userId, productId);
		String fileName = String.format("%s/%s",String.valueOf(UUID.randomUUID()), file.getOriginalFilename());
		System.out.println(path+" "+fileName);
		try {
			fileStore.uploadImage(path, fileName, Optional.of(metadata), file.getInputStream());
//			System.out.println(amazonS3.getUrl(path, fileName));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to upload file", e);
		}
		Image image = Image.builder().productId(productId).fileName(fileName).s3BucketPath(String.valueOf(amazonS3.getUrl(path, fileName))).build();
		return imageRepository.saveAndFlush(image);
	}

	public List<Image> getAllImages(Long productId, Long userId) throws DataNotFoundExeception {
		List<Image> images=imageRepository.findImageByProductId(productId);
		if(images==null)
			throw new DataNotFoundExeception("No Product with given Id");
		return images;
	}

	public Image getImage(Long productId, Long userId, Long imageId) throws DataNotFoundExeception, BadInputException {
		Optional<Image> imgObj=imageRepository.findById(imageId);
		if(imgObj.isEmpty())
			throw new DataNotFoundExeception("No Image with given Id");
		System.out.println(imgObj.get().getProductId()+" "+productId);
		if(imgObj.get().getProductId()!=productId)
			throw new BadInputException("ProductId and imageId won't match");
		return imgObj.get();
	}

	public Image deleteImage(Long productId, Long userId, Long imageId)  throws DataNotFoundExeception, BadInputException {
		Optional<Image> imgObj=imageRepository.findById(imageId);
		if(imgObj.isEmpty())
			throw new DataNotFoundExeception("No Image with given Id");
		System.out.println(imgObj.get().getProductId()+" "+productId);
		if(imgObj.get().getProductId()!=productId)
			throw new BadInputException("ProductId and imageId won't match");
		System.out.println(imgObj.get().getS3BucketPath()+" "+imgObj.get().getFileName());
		String path = String.format("%s/%s/%s", bucketName, userId, productId);
		fileStore.deleteFile(path,imgObj.get().getFileName());
		return imgObj.get();
//		URL url;
//		try {
//			url = new URL(imgObj.get().getS3BucketPath());
//			System.out.println(url.getPath());
//			fileStore.deleteFile(url.getPath(),imgObj.get().getFileName());
//			return imgObj.get();
//		} catch (MalformedURLException e) {
//			throw new IllegalStateException("Url Parsing error");
//		}
		
	}
	
	public void deleteImageByProductId(Long productId, Long userId)  throws DataNotFoundExeception {
//		System.out.println(imgObj.get().getS3BucketPath()+" "+imgObj.get().getFileName());
		String path = String.format("%s/%s/%s", bucketName, userId, productId);
		fileStore.deleteFile(path,"/");
	}

}
