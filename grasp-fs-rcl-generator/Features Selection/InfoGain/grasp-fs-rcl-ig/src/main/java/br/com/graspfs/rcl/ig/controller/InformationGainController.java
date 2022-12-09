package br.com.graspfs.rcl.ig.controller;

import br.com.graspfs.rcl.ig.dto.DataSolution;
import br.com.graspfs.rcl.ig.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.ig.service.InformationGainService;
import br.com.graspfs.rcl.ig.util.SelectionFeaturesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ig")
@RequiredArgsConstructor
public class InformationGainController {
        private final KafkaSolutionsProducer InformationGainProducer;


    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            InformationGainService informationGainService = new InformationGainService();
            data = informationGainService.doIG();
            int k = 0;
            while (k <100){
                informationGainService.GenerationSolutions(data, 5);
                InformationGainProducer.send(data);
                k++;
            }
        }catch(IllegalArgumentException ex){
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body(data);
    }



}
