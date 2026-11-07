import uk.me.g4dpz.satellite.GroundStationPosition;
import uk.me.g4dpz.satellite.PassPredictor;
import uk.me.g4dpz.satellite.SatPassTime;
import uk.me.g4dpz.satellite.TLE;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Prints the passes predict4java finds, one per line, in the same format as
 * predict4cpp's pass_dump tool, so the two can be compared with diff.
 *
 * Arguments: line1 line2 latitude longitude altitudeMetres startEpochSeconds hours windBack
 * Output:    startEpoch endEpoch tcaEpoch pole aosAzimuth losAzimuth maxElevation(4dp)
 */
public class ComparePassPredictor {
    public static void main(String[] args) throws Exception {
        final String[] tleLines = {"SAT", args[0], args[1]};
        final TLE tle = new TLE(tleLines);
        final GroundStationPosition qth = new GroundStationPosition(
                Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        final long startSeconds = Long.parseLong(args[5]);
        final int hours = Integer.parseInt(args[6]);
        final boolean windBack = Boolean.parseBoolean(args[7]);

        final PassPredictor predictor = new PassPredictor(tle, qth);
        final List<SatPassTime> passes = predictor.getPasses(new Date(startSeconds * 1000L), hours, windBack);

        for (final SatPassTime p : passes) {
            System.out.println(String.format(Locale.ROOT, "%d %d %d %s %d %d %.4f",
                    p.getStartTime().getTime() / 1000L,
                    p.getEndTime().getTime() / 1000L,
                    p.getTCA().getTime() / 1000L,
                    p.getPolePassed(),
                    p.getAosAzimuth(),
                    p.getLosAzimuth(),
                    p.getMaxEl()));
        }
    }
}
