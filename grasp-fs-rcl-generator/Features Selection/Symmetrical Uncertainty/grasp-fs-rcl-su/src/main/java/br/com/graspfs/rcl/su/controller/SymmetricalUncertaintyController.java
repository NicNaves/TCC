package br.com.graspfs.rcl.su.controller;

import br.com.graspfs.rcl.su.dto.DataSolution;
import br.com.graspfs.rcl.su.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.su.service.SymmetricalUncertaintyService;
import br.com.graspfs.rcl.su.util.SelectionFeaturesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/su")
@RequiredArgsConstructor
public class SymmetricalUncertaintyController {
        private final KafkaSolutionsProducer SymmetricalUncertaintyProducer;


    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            SymmetricalUncertaintyService symmetricalUncertaintyService = new SymmetricalUncertaintyService();
            data = symmetricalUncertaintyService.doSU();
            int k = 0;
            while (k <100) {
                symmetricalUncertaintyService.GenerationSolutions(data,5);
                SymmetricalUncertaintyProducer.send(data);
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
