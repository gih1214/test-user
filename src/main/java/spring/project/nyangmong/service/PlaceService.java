package spring.project.nyangmong.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import spring.project.nyangmong.domain.image.ImageRepository;
import spring.project.nyangmong.domain.image.PublicDataImage;
import spring.project.nyangmong.domain.places.PlaceRepository;
import spring.project.nyangmong.domain.places.Places;
import spring.project.nyangmong.web.dto.craw.PlaceDto;

@RequiredArgsConstructor
@Service
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final ImageRepository imageRepository;

    public Places 상세보기(Integer contentSeq) {
        Optional<Places> placesOp = placeRepository.findById(contentSeq);

        if (placesOp.isPresent()) {
            return placesOp.get();
        } else {
            throw new RuntimeException("해당 관광정보를 찾을 수 없습니다");
        }
    }

    public Places 분류검색(String partName) {
        Places placesOp = placeRepository.searchPartName(partName);
        return placesOp;
    }

    public boolean 옵션표시(String yesOrNO) {
        if (yesOrNO.equals("Y")) {
            return true;
        }
        return false;
    }
}