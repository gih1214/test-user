package spring.project.nyangmong.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import spring.project.nyangmong.domain.image.ImageRepository;
import spring.project.nyangmong.domain.image.PublicDataImage;
import spring.project.nyangmong.domain.places.PlaceRepository;
import spring.project.nyangmong.domain.places.Places;
import spring.project.nyangmong.service.PlaceService;
import spring.project.nyangmong.util.ContentSeqDownload;
import spring.project.nyangmong.web.dto.craw.PlaceDto;
import spring.project.nyangmong.web.dto.craw.Result;
import spring.project.nyangmong.web.dto.places.ImageListDto;

@RequiredArgsConstructor
@Controller
public class PlaceController {
    private final PlaceService placeService;
    private final PlaceRepository placeRepository;
    private final ImageRepository imageRepository;
    private final HttpSession session;

    // 상세보기

    @GetMapping("/place/{contentSeq}")
    public String detailPlaces(@PathVariable Integer contentSeq, Model model) {
        Places places = placeService.상세보기(contentSeq);
        List<PublicDataImage> imageList = imageRepository.ImagecontentSeq(contentSeq);

        ImageListDto dto = new ImageListDto();
        dto.setPublicDataImage(imageList);
        dto.setBathFlagShow(placeService.옵션표시(places.getBathFlag()));
        dto.setParkingFlagShow(placeService.옵션표시(places.getParkingFlag()));
        dto.setEntranceFlagShow(placeService.옵션표시(places.getEntranceFlag()));
        dto.setEmergencyFlagShow(placeService.옵션표시(places.getEmergencyFlag()));
        dto.setProvisionFlagShow(placeService.옵션표시(places.getProvisionFlag()));
        dto.setInOutFlagShow(placeService.옵션표시(places.getInOutFlag()));
        dto.setPetFlagShow(placeService.옵션표시(places.getPetFlag()));
        // List<PlaceDto> placesDto = new ArrayList<>();

        model.addAttribute("imageList", dto);
        model.addAttribute("places", places);
        return "pages/detail/placeDetail";
    }

    // 검색- 쿼리스트링 이용가능
    // 단, 직접적으로 검색을 하려 할 시 , 한글이 깨지는 현상 발생중
    // 받아온 결과가 한개가 아니라 여러개라서 에러가 터지는 상태.
    @GetMapping("/place/search")
    public String searchPartName(@RequestParam String partName, Model model) {
        Places places = placeService.분류검색(partName);
        // long count = placeRepository.countPartName(partName);
        // model.addAttribute("count", count);
        model.addAttribute("places", places);

        if (partName.equals("관광지")) {
            return "pages/detail/spotList";
        } else if (partName.equals("동물병원")) {
            return "pages/detail/hospitalList";
        } else if (partName.equals("식음료")) {
            return "pages/detail/cafeList";
        } else if (partName.equals("체험")) {
            return "pages/detail/activityList";
        } else if (partName.equals("숙박")) {
            return "pages/detail/hotelList";
        } else {
            throw new RuntimeException("해당 관광정보를 찾을 수 없습니다");
        }
    }

    // 데이터베이스 받아오는 url 들어갈때 시간이 많이 걸립니다.
    // 모두 받아오고 둘러보기로 이동.
    @GetMapping("/list")
    public String download(Model model) {

        for (int k = 1; k < 6; k++) {

            List<Integer> contentSeqList = new ContentSeqDownload().contentSeqDown(k);

            System.out.println(contentSeqList);

            RestTemplate rt = new RestTemplate();
            for (int j = 0; j < contentSeqList.size(); j++) {

                String url = "http://www.pettravel.kr/api/detailSeqPart.do?partCode=PC0" + k + "&contentNum="
                        + contentSeqList.get(j);

                // System.out.println("url : " + url);

                Result[] responseDtos = rt.getForObject(url, Result[].class);

                Result responseDto = responseDtos[0];

                // 다운 받은 것
                PlaceDto placeDto = responseDto.getResultList(); // 한건
                // System.out.println("Dto : " + placeDto);
                // Place 엔티티에 옮김
                Places place = Places.builder()
                        .contentSeq(placeDto.getContentSeq())
                        .areaName(placeDto.getAreaName())
                        .partName(placeDto.getPartName())
                        .title(placeDto.getTitle())
                        .keyword(placeDto.getKeyword())
                        .address(placeDto.getAddress())
                        .tel(placeDto.getTel())
                        .latitude(placeDto.getLatitude())
                        .longitude(placeDto.getLongitude())
                        .usedTime(placeDto.getUsedTime())
                        .homePage(placeDto.getHomePage())
                        .content(placeDto.getContent())
                        .provisionSupply(placeDto.getProvisionSupply())
                        .petFacility(placeDto.getPetFacility())
                        .restaurant(placeDto.getRestaurant())
                        .parkingLog(placeDto.getParkingLog())
                        .mainFacility(placeDto.getMainFacility())
                        .usedCost(placeDto.getUsedCost())
                        .policyCautions(placeDto.getPolicyCautions())
                        .emergencyResponse(placeDto.getEmergencyResponse())
                        .memo(placeDto.getMemo())
                        .bathFlag(placeDto.getBathFlag())
                        .provisionFlag(placeDto.getProvisionFlag())
                        .petFlag(placeDto.getPetFlag())
                        .petWeight(placeDto.getPetWeight())
                        .petBreed(placeDto.getPetBreed())
                        .emergencyFlag(placeDto.getEmergencyFlag())
                        .entranceFlag(placeDto.getEntranceFlag())
                        .parkingFlag(placeDto.getParkingFlag())
                        .inOutFlag(placeDto.getInOutFlag())
                        // 추가
                        .build();

                // System.out.println(place);

                Places placeEntity = placeRepository.save(place); // id 찾으려구

                // PC05 => 병원 이미지 하나도 없어서 이미지 저장 안해야됨 => 놔두면 nullpointerexception 발생
                if (k != 5) {

                    List<PublicDataImage> images = new ArrayList<>();
                    for (int i = 0; i < placeDto.getImageList().size(); i++) {
                        PublicDataImage image = PublicDataImage.builder()
                                .imgurl(placeDto.getImageList().get(i).getImage())
                                .places(placeEntity) // <- placeEntity
                                .build();
                        images.add(image);
                    }

                    imageRepository.saveAll(images);
                }

            }
        }
        return "pages/list/outlineList";
    }

}