package eu.h2020.symbiote.tm.controllers;

import eu.h2020.symbiote.tm.services.QoSService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("qos")
public class QoSController {

    @Autowired
    private QoSService qoSService;

    @ApiOperation(value = "Update Interval")
    @RequestMapping(path = "/updateInterval/{newInterval}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateInterval(
            @ApiParam(value = "New Interval") @PathVariable String newInterval
    ){
        newInterval = newInterval.replace('#', '/');
        qoSService.updateIntervalPlatformReputationUpdate(newInterval);
        return new ResponseEntity<>("Interval successfully updated", HttpStatus.OK);
    }
}
