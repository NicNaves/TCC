package br.com.graspfs.rcl.or.controller;

import br.com.graspfs.rcl.or.dto.DataSolution;
import br.com.graspfs.rcl.or.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.or.service.OneRService;
import br.com.graspfs.rcl.or.util.SelectionFeaturesUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/or")
@RequiredArgsConstructor
public class OnerRController {
        private final KafkaSolutionsProducer OneRProducer;


    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            OneRService onerRService = new OneRService();
            data = onerRService.doOR();
            int k = 0;
            while (k <100) {
                onerRService.GenerationSolutions(data,5);
                OneRProducer.send(data);
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
