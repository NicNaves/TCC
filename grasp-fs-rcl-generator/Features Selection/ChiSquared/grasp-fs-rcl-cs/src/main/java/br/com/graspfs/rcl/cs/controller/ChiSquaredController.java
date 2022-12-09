package br.com.graspfs.rcl.cs.controller;

import br.com.graspfs.rcl.cs.dto.DataSolution;
import br.com.graspfs.rcl.cs.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.cs.service.ChiSquaredService;
import br.com.graspfs.rcl.cs.util.SelectionFeaturesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cs")
@RequiredArgsConstructor
public class ChiSquaredController {
        private final KafkaSolutionsProducer ChiSquaredProducer;


    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            ChiSquaredService chiSquaredService = new ChiSquaredService();
            data = chiSquaredService.doCS();
            int k = 0;
            while (k <100) {
                chiSquaredService.GenerationSolutions(data, 5);
                ChiSquaredProducer.send(data);
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
