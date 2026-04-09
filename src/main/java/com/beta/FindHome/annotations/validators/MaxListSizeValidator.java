package com.beta.FindHome.annotations.validators;

import com.beta.FindHome.annotations.MaxListSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MaxListSizeValidator implements ConstraintValidator<MaxListSize, List<MultipartFile>> {

    private int max;

    @Override
    public void initialize(MaxListSize constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        if (files == null || files.isEmpty()) {
            return true; // Null or empty lists are valid
        }

        System.out.println("Validating file list: size=" + files.size() + ", max=" + max);

        if (files.size() <= max) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("You can upload a maximum of " + max + " images.")
                .addConstraintViolation();

        return false;
    }

}
