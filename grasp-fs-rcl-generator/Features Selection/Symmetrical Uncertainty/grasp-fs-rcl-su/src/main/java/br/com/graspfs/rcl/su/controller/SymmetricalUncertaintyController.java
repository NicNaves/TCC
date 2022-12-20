package br.com.graspfs.rcl.su.controller;

import br.com.graspfs.rcl.su.dto.DataSolution;
import br.com.graspfs.rcl.su.producer.KafkaSolutionsProducer;
import br.com.graspfs.rcl.su.service.SymmetricalUncertaintyService;
import br.com.graspfs.rcl.su.util.SelectionFeaturesUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.BufferedWriter;
import java.io.FileWriter;

@Controller
@RequestMapping("/su")
@RequiredArgsConstructor
public class SymmetricalUncertaintyController {
        private final KafkaSolutionsProducer SymmetricalUncertaintyProducer;

    public static BufferedWriter br;
    public boolean firstTime = true;
    private final Logger logg = LoggerFactory.getLogger(SymmetricalUncertaintyController.class);
    @PostMapping
    public ResponseEntity<DataSolution> createMessage() throws Exception {
        var data = SelectionFeaturesUtils.createData();
        try{
            SymmetricalUncertaintyService symmetricalUncertaintyService = new SymmetricalUncertaintyService();
            data = symmetricalUncertaintyService.doSU();
            int k = 0;
            br = new BufferedWriter(new FileWriter("SU_METRICS_30",true));
            if(firstTime) {
                br.write("solutionFeatures;f1Score;neighborhood;iterationNeighborhood;localSearch;iterationLocalSearch;runnigTime");
                br.newLine();
                firstTime = false;
            }
            while (k <10000) {
                symmetricalUncertaintyService.GenerationSolutions(data,5, br);
                SymmetricalUncertaintyProducer.send(data);
                k++;
                logg.info(String.valueOf(k));
            }
            br.close();
        }catch(IllegalArgumentException ex){
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).body(data);
    }

}
