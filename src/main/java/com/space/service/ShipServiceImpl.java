package com.space.service;

import com.space.exception.BadRequestException;
import com.space.exception.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ShipServiceImpl implements ShipService {

    @Autowired
    public ShipRepository shipRepository;

    @Override
    public List<Ship> findAll(Specification<Ship> specification) {
        return shipRepository.findAll(specification);
    }

    @Override
    public Page<Ship> findAll(Specification specification, Pageable pageable) {
        return shipRepository.findAll(specification, pageable);
    }

    @Override
    public Ship get(Long id) {
        checkId(id);
        Optional<Ship> ship = shipRepository.findById(id);
        if (!ship.isPresent()) throw new NotFoundException("No ships with such id.");
        return ship.get();
    }

    @Override
    public Ship save(Ship ship) {
        if (ship.getSpeed() == null || ship.getProdDate() == null || ship.getPlanet() == null ||
                ship.getShipType() == null || ship.getName() == null || ship.getCrewSize() == null) {
            throw new BadRequestException("Not enough parameters in request.");
        }
        checkAllShipFields(ship);
        ship.setSpeed((double) Math.round(ship.getSpeed() * 100) / 100);
        if (ship.isUsed() == null) {
            ship.setUsed(false);
        }
        ship.setRating(calculateRating(ship));
        return shipRepository.save(ship);
    }

    @Override
    public Ship update(Long id, Ship ship) {
        checkId(id);
        checkAllShipFields(ship);

        Ship foundedShip = get(id);

        if (ship.getName() != null) {
            foundedShip.setName(ship.getName());
        }
        if (ship.getPlanet() != null) {
            foundedShip.setPlanet(ship.getPlanet());
        }
        if (ship.getProdDate() != null) {
            foundedShip.setProdDate(ship.getProdDate());
        }
        if (ship.getSpeed() != null) {
            foundedShip.setSpeed((double) Math.round(ship.getSpeed() * 100) / 100);
        }
        if (ship.getCrewSize() != null) {
            foundedShip.setCrewSize(ship.getCrewSize());
        }
        if (ship.getShipType() != null) {
            foundedShip.setShipType(ship.getShipType());
        }
        if (ship.isUsed() != null) {
            foundedShip.setUsed(ship.isUsed());
        }
        if (ship.isUsed() != null || ship.getSpeed() != null || ship.getProdDate() != null) {
            foundedShip.setRating(calculateRating(foundedShip));
        }
        return shipRepository.save(foundedShip);
    }

    @Override
    public void deleteById(Long id) {
        checkId(id);
        Ship foundedShip = get(id);
        shipRepository.deleteById(foundedShip.getId());
    }

    @Override
    public Specification<Ship> filterByName(String name) {
        return (root, query, criteriaBuilder) -> name == null ? null : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Ship> filterByPlanet(String planet) {
        return (root, query, criteriaBuilder) -> planet == null ? null : criteriaBuilder.like(root.get("planet"), "%" + planet + "%");
    }

    @Override
    public Specification<Ship> filterByShipType(ShipType shipType) {
        return (root, query, criteriaBuilder) -> shipType == null ? null : criteriaBuilder.equal(root.get("shipType"), shipType);
    }

    @Override
    public Specification<Ship> filterByDate(Long afterMillisecons, Long beforeMilliseconds) {
        return (root, query, criteriaBuilder) -> {
            if (afterMillisecons == null && beforeMilliseconds == null) return null;

            if (afterMillisecons == null) {
                Date dateBefore = new Date(beforeMilliseconds);
                return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), dateBefore);
            }
            if (beforeMilliseconds == null) {
                Date dateAfter = new Date(afterMillisecons);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), dateAfter);
            }
            Date dateBefore = new Date(beforeMilliseconds);
            Date dateAfter = new Date(afterMillisecons);
            return criteriaBuilder.between(root.get("prodDate"), dateAfter, dateBefore);
        };
    }

    @Override
    public Specification<Ship> filterByUsage(Boolean isUsed) {
        return (root, query, criteriaBuilder) -> {
            if (isUsed == null) return null;

            return isUsed ? criteriaBuilder.isTrue(root.get("isUsed")) : criteriaBuilder.isFalse(root.get("isUsed"));
        };
    }

    @Override
    public Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed) {
        return (root, query, criteriaBuilder) -> {
            if (minSpeed == null && maxSpeed == null) return null;
            if (minSpeed == null) return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed);
            if (maxSpeed == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed);

            return criteriaBuilder.between(root.get("speed"), minSpeed, maxSpeed);
        };
    }

    @Override
    public Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
        return (root, query, criteriaBuilder) -> {
            if (minCrewSize == null && maxCrewSize == null) return null;
            if (minCrewSize == null) return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize);
            if (maxCrewSize == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize);

            return criteriaBuilder.between(root.get("crewSize"), minCrewSize, maxCrewSize);
        };
    }

    @Override
    public Specification<Ship> filterByRating(Double minRating, Double maxRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null && maxRating == null) return null;
            if (minRating == null) return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating);
            if (maxRating == null) return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);

            return criteriaBuilder.between(root.get("rating"), minRating, maxRating);
        };
    }

    private void checkAllShipFields(Ship ship) {
        if (ship.getCrewSize() != null && (ship.getCrewSize() < 0 || ship.getCrewSize() > 9999)) {
            throw new BadRequestException("Crew size not in range.");
        }
        if (ship.getName() != null && (ship.getName().isEmpty() || ship.getName().length() > 50)) {
            throw new BadRequestException("Ship name is empty or it's length is more than 50 symbols.");
        }
        if (ship.getSpeed() != null && (ship.getSpeed() <= 0 || ship.getSpeed() >= 1)) {
            throw new BadRequestException("Ship speed not in range.");
        }
        if (ship.getProdDate() != null && (ship.getProdDate().getTime() < 26192235600000L || ship.getProdDate().getTime() > 33134734800000L)) {
            throw new BadRequestException("Ship produce date not in range.");
        }
        if (ship.getPlanet() != null && (ship.getPlanet().isEmpty() || ship.getPlanet().length() > 50)) {
            throw new BadRequestException("Ship planet is empty or it's length is more than 50 symbols.");
        }
        if (ship.getShipType() != null && (!ShipType.isMember(ship.getShipType().name()))) {
            throw new BadRequestException("Ship type is not found.");
        }
    }

    private void checkId(Long id) {
        if (id == null || id <= 0) throw new BadRequestException("Id is not correct.");
    }

    private Double calculateRating(Ship ship) {
        double k;
        if (ship.isUsed()) {
            k = 0.5;
        } else {
            k = 1;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(ship.getProdDate());
        return (double) Math.round((80 * ship.getSpeed() * k) / (3019 - cal.get(Calendar.YEAR) + 1) * 100) / 100;
    }
}
