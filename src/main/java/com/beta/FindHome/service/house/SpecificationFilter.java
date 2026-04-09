package com.beta.FindHome.service.house;

import com.beta.FindHome.dto.property.PropertyRequestDTO;
import com.beta.FindHome.model.Flat;
import com.beta.FindHome.model.House;
import com.beta.FindHome.model.Room;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationFilter {

    public static Specification<House> houseFilterByCriteria(PropertyRequestDTO propertyRequestDTO) {
        return (Root<House> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Filter by landlord ID
            if (propertyRequestDTO.getLandlordId() != null) {
                predicate = criteriaBuilder.equal(root.get("landlordId").get("id"), propertyRequestDTO.getLandlordId());
            }

            // Filter by minimum price
            if (propertyRequestDTO.getMinPrice() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("housePrice"), propertyRequestDTO.getMinPrice()));
            }

            // Filter by maximum price
            if (propertyRequestDTO.getMaxPrice() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("housePrice"), propertyRequestDTO.getMaxPrice()));
            }

            // Sorting
            String sortBy = propertyRequestDTO.getSortBy(); // newest, oldest, price-asc, price-desc
            if (sortBy != null) {
                switch (sortBy) {
                    case "newest":
                        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                        break;
                    case "oldest":
                        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createdAt")));
                        break;
                }
            }

            // Filter by district (case-insensitive)
            if (propertyRequestDTO.getDistrict() != null && !propertyRequestDTO.getDistrict().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("district")),
                                "%" + propertyRequestDTO.getDistrict().toLowerCase() + "%"
                        )
                );
            }

            // Filter by city (case-insensitive)
            if (propertyRequestDTO.getCity() != null && !propertyRequestDTO.getCity().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("city")),
                                "%" + propertyRequestDTO.getCity().toLowerCase() + "%"
                        )
                );
            }

            // Filter by ward (case-insensitive)
            if (propertyRequestDTO.getWard() != null && !propertyRequestDTO.getWard().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("ward")),
                                "%" + propertyRequestDTO.getWard().toLowerCase() + "%"
                        )
                );
            }

            // Filter by tole (case-insensitive)
            if (propertyRequestDTO.getTole() != null && !propertyRequestDTO.getTole().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("tole")),
                                "%" + propertyRequestDTO.getTole().toLowerCase() + "%"
                        )
                );
            }

            //isVerified
            if(propertyRequestDTO.getIsVerified() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isVerified"), propertyRequestDTO.getIsVerified()));
            }

            // Filter by availability
            if (propertyRequestDTO.getIsAvailable() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isAvailable"), propertyRequestDTO.getIsAvailable()));
            }

            //filter by deletion
            if(propertyRequestDTO.getIsDeleted() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isDeleted"), propertyRequestDTO.getIsDeleted()));
            }

            // Filter by amenities
            if (propertyRequestDTO.getHasParking() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasParking"), propertyRequestDTO.getHasParking()));
            }
            if (propertyRequestDTO.getHasWifi() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasWifi"), propertyRequestDTO.getHasWifi()));
            }
            if (propertyRequestDTO.getHasSecurityStaff() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasSecurityStaff"), propertyRequestDTO.getHasSecurityStaff()));
            }
            if (propertyRequestDTO.getHasUnderGroundWaterTank() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasUnderGroundWaterTank"), propertyRequestDTO.getHasUnderGroundWaterTank()));
            }
            if (propertyRequestDTO.getHasTV() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasTV"), propertyRequestDTO.getHasTV()));
            }
            if (propertyRequestDTO.getHasCCTV() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasCCTV"), propertyRequestDTO.getHasCCTV()));
            }
            if (propertyRequestDTO.getHasAC() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasAC"), propertyRequestDTO.getHasAC()));
            }
            if (propertyRequestDTO.getHasFridge() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasFridge"), propertyRequestDTO.getHasFridge()));
            }
            if (propertyRequestDTO.getHasBalcony() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasBalcony"), propertyRequestDTO.getHasBalcony()));
            }
            if (propertyRequestDTO.getHasWater() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasWater"), propertyRequestDTO.getHasWater()));
            }
            if (propertyRequestDTO.getHasSolarWaterHeater() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasSolarWaterHeater"), propertyRequestDTO.getHasSolarWaterHeater()));
            }
            if (propertyRequestDTO.getHasFan() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasFan"), propertyRequestDTO.getHasFan()));
            }

            return predicate;
        };
    }

    public static Specification<Room> roomFilterByCriteria(PropertyRequestDTO propertyRequestDTO){
        return ( Root<Room> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Filter by landlord ID
            if (propertyRequestDTO.getLandlordId() != null) {
                predicate = criteriaBuilder.equal(root.get("landlordId").get("id"), propertyRequestDTO.getLandlordId());
            }

            // Filter by minimum price
            if (propertyRequestDTO.getMinPrice() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("roomPrice"), propertyRequestDTO.getMinPrice()));
            }

            // Filter by maximum price
            if (propertyRequestDTO.getMaxPrice() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("roomPrice"), propertyRequestDTO.getMaxPrice()));
            }

            // Sorting
            String sortBy = propertyRequestDTO.getSortBy(); // newest, oldest, price-asc, price-desc
            if (sortBy != null) {
                switch (sortBy) {
                    case "newest":
                        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                        break;
                    case "oldest":
                        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createdAt")));
                        break;
                }
            }

            // Filter by district (case-insensitive)
            if (propertyRequestDTO.getDistrict() != null && !propertyRequestDTO.getDistrict().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("district")),
                                "%" + propertyRequestDTO.getDistrict().toLowerCase() + "%"
                        )
                );
            }

            //isVerified
            if(propertyRequestDTO.getIsVerified() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isVerified"), propertyRequestDTO.getIsVerified()));
            }

            // Filter by availability
            if (propertyRequestDTO.getIsAvailable() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isAvailable"), propertyRequestDTO.getIsAvailable()));
            }

            //filter by deletion
            if(propertyRequestDTO.getIsDeleted() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isDeleted"), propertyRequestDTO.getIsDeleted()));
            }

            // Filter by city (case-insensitive)
            if (propertyRequestDTO.getCity() != null && !propertyRequestDTO.getCity().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("city")),
                                "%" + propertyRequestDTO.getCity().toLowerCase() + "%"
                        )
                );
            }

            // Filter by ward (case-insensitive)
            if (propertyRequestDTO.getWard() != null && !propertyRequestDTO.getWard().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("ward")),
                                "%" + propertyRequestDTO.getWard().toLowerCase() + "%"
                        )
                );
            }

            // Filter by tole (case-insensitive)
            if (propertyRequestDTO.getTole() != null && !propertyRequestDTO.getTole().isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("tole")),
                                "%" + propertyRequestDTO.getTole().toLowerCase() + "%"
                        )
                );
            }

            // Filter by amenities
            if (propertyRequestDTO.getHasParking() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasParking"), propertyRequestDTO.getHasParking()));
            }
            if (propertyRequestDTO.getHasWifi() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasWifi"), propertyRequestDTO.getHasWifi()));
            }
            if (propertyRequestDTO.getHasSecurityStaff() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasSecurityStaff"), propertyRequestDTO.getHasSecurityStaff()));
            }
            if (propertyRequestDTO.getHasUnderGroundWaterTank() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasUnderGroundWaterTank"), propertyRequestDTO.getHasUnderGroundWaterTank()));
            }
            if (propertyRequestDTO.getHasTV() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasTV"), propertyRequestDTO.getHasTV()));
            }
            if (propertyRequestDTO.getHasCCTV() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasCCTV"), propertyRequestDTO.getHasCCTV()));
            }
            if (propertyRequestDTO.getHasAC() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasAC"), propertyRequestDTO.getHasAC()));
            }
            if (propertyRequestDTO.getHasFridge() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasFridge"), propertyRequestDTO.getHasFridge()));
            }
            if (propertyRequestDTO.getHasBalcony() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasBalcony"), propertyRequestDTO.getHasBalcony()));
            }
            if (propertyRequestDTO.getHasWater() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasWater"), propertyRequestDTO.getHasWater()));
            }
            if (propertyRequestDTO.getHasSolarWaterHeater() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasSolarWaterHeater"), propertyRequestDTO.getHasSolarWaterHeater()));
            }
            if (propertyRequestDTO.getHasFan() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasFan"), propertyRequestDTO.getHasFan()));
            }

            // Filter by amenities
            return predicate;

           };
    }

    public static Specification<Flat> flatFilterByCriteria(
            PropertyRequestDTO propertyRequestDTO
    ) {
        return (Root<Flat> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();


            // Filter by landlord ID
            if (propertyRequestDTO.getLandlordId() != null) {
                predicate = criteriaBuilder.equal(root.get("landlordId").get("id"), propertyRequestDTO.getLandlordId());
            }

            // Filter by minimum price
            if (propertyRequestDTO.getMinPrice() != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("flatPrice"),
                                propertyRequestDTO.getMinPrice()));
            }

            // Filter by maximum price
            if (propertyRequestDTO.getMaxPrice() != null) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("flatPrice"),
                                propertyRequestDTO.getMaxPrice()));
            }

            // Sorting
            String sortBy = propertyRequestDTO.getSortBy(); // newest, oldest, price-asc, price-desc
            if (sortBy != null) {
                switch (sortBy) {
                    case "newest":
                        criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                        break;
                    case "oldest":
                        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("createdAt")));
                        break;
                }
            }

            // Filter by district (case-insensitive)
            if (propertyRequestDTO.getDistrict() != null && !propertyRequestDTO.getDistrict().isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("district")),
                                "%" + propertyRequestDTO.getDistrict().toLowerCase() + "%"
                        )
                );
            }

            //isVerified
            if(propertyRequestDTO.getIsVerified() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isVerified"), propertyRequestDTO.getIsVerified()));
            }

            // Filter by availability
            if (propertyRequestDTO.getIsAvailable() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isAvailable"), propertyRequestDTO.getIsAvailable()));
            }

            //filter by deletion
            if(propertyRequestDTO.getIsDeleted() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isDeleted"), propertyRequestDTO.getIsDeleted()));
            }

            // Filter by city (case-insensitive)
            if (propertyRequestDTO.getCity() != null && !propertyRequestDTO.getCity().isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("city")),
                                "%" + propertyRequestDTO.getCity().toLowerCase() + "%"
                        )
                );
            }

            // Filter by ward (case-insensitive)
            if (propertyRequestDTO.getWard() != null && !propertyRequestDTO.getWard().isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("ward")),
                                "%" + propertyRequestDTO.getWard().toLowerCase() + "%"
                        )
                );
            }

            // Filter by tole (case-insensitive)
            if (propertyRequestDTO.getTole() != null && !propertyRequestDTO.getTole().isEmpty()) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("tole")),
                                "%" + propertyRequestDTO.getTole().toLowerCase() + "%"
                        )
                );
            }

            // Filter by amenities
            if (propertyRequestDTO.getHasParking() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasParking"), propertyRequestDTO.getHasParking()));
            }
            if (propertyRequestDTO.getHasWifi() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasWifi"), propertyRequestDTO.getHasWifi()));
            }
            if (propertyRequestDTO.getHasSecurityStaff() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasSecurityStaff"), propertyRequestDTO.getHasSecurityStaff()));
            }
            if (propertyRequestDTO.getHasUnderGroundWaterTank() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasUnderGroundWaterTank"), propertyRequestDTO.getHasUnderGroundWaterTank()));
            }
            if (propertyRequestDTO.getHasTV() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasTV"), propertyRequestDTO.getHasTV()));
            }
            if (propertyRequestDTO.getHasCCTV() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasCCTV"), propertyRequestDTO.getHasCCTV()));
            }
            if (propertyRequestDTO.getHasAC() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasAC"), propertyRequestDTO.getHasAC()));
            }
            if (propertyRequestDTO.getHasFridge() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasFridge"), propertyRequestDTO.getHasFridge()));
            }
            if (propertyRequestDTO.getHasBalcony() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasBalcony"), propertyRequestDTO.getHasBalcony()));
            }
            if (propertyRequestDTO.getHasWater() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasWater"), propertyRequestDTO.getHasWater()));
            }
            if (propertyRequestDTO.getHasSolarWaterHeater() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasSolarWaterHeater"), propertyRequestDTO.getHasSolarWaterHeater()));
            }
            if (propertyRequestDTO.getHasFan() != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.join("amenities").get("hasFan"), propertyRequestDTO.getHasFan()));
            }
            return predicate;
        };
    }



}
