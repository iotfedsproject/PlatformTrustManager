package eu.h2020.symbiote.tm.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QoS {

    private double availability;

    private double response_time;

    private double precision;
}
