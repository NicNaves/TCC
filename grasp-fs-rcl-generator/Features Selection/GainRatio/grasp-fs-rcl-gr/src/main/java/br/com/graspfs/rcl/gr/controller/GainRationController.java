package br.com.graspfs.rcl.gr.controller;

import br.com.graspfs.rcl.gr.dto.DataSolution;
import br.com.graspfs.rcl.gr.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.gr.service.GainRationService;
import br.com.graspfs.rcl.gr.util.SelectionFeaturesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;

@Controller
@RequestMapping("/gr")
@RequiredArgsConstructor
public class GainRationController {
        private final KafkaSolutionsProducer GainRationProducer;


    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            GainRationService gainRationService = new GainRationService();
            data = gainRationService.doGainRation();
            int k = 0;
            while( k < 100){
                gainRationService.GenerationSolutions(data, 5);
                GainRationProducer.send(data);
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
