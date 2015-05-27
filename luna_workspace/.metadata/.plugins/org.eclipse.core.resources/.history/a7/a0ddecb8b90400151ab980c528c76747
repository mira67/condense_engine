package condense_engine;

import helper_classes.Tools;

/**
 * Downsampling condense methods library
 * 1-triangle
 * 2-extreme minimum and maximum
 * 3-band limited down sampling
 * @author Qi Liu, modified
 */
public class Downsampling{
	
	/* Function: largestTriangleThreeBuckets
	 * input- data: x-index, y-value; threshold: how many to keep
	 * output- sampled data with index
	 * @author Benjamin Green; Modified by Qi
	 */
	public static Number[][] largestTriangleThreeBuckets(Number[][] data, Integer threshold) {
		Number[][] sampled = new Number[threshold][];
		if (data == null) {
			throw new NullPointerException("Cannot cope with a null data input array.");
		}
		if (threshold <= 2) {
			throw new RuntimeException("What am I supposed to do with that?");
		}
		if (data.length <= 2 || data.length <= threshold) {
			return data;
		}
		int sampled_index = 0;
		//split the data in equal size number of buckets
		double every = (double)(data.length - 2) / (double)(threshold - 2);
		System.out.println(": " + every);
		int a = 0, next_a = 0;
		Number[] max_area_point = null;
		double max_area, area;
		
		sampled[sampled_index++] = data[a];
		
		for (int i = 0; i < threshold - 2; i++) {
			double avg_x = 0.0D, avg_y = 0.0D;
			int avg_range_start = (int)Math.floor((i+1)*every) + 1;
			int avg_range_end = (int)Math.floor((i+2)*every) + 1;
			avg_range_end = avg_range_end < data.length ? avg_range_end : data.length;
			int avg_range_length = (int)(avg_range_end - avg_range_start);
			while (avg_range_start < avg_range_end) {
				avg_x = avg_x + data[avg_range_start][0].doubleValue();
				avg_y += data[avg_range_start][1].doubleValue();
				avg_range_start ++;
			}
			avg_x /= avg_range_length;
			avg_y /= avg_range_length;
			
			int range_offs = (int)Math.floor((i + 0) * every) + 1;
			int range_to = (int)Math.floor((i + 1) * every) + 1;
			
			//point to evaluate
			double point_a_x = data[a][0].doubleValue();
			double point_a_y = data[a][1].doubleValue();
			
			max_area = area = -1;
			
			while (range_offs < range_to) {//points in a bucket to be evaluated
				/*area = Math.abs(
						(point_a_x - avg_x) * (data[range_offs][1].doubleValue() - point_a_y) -
						(point_a_x - data[range_offs][0].doubleValue()) * (avg_y - point_a_y)
						) * 0.5D;*/
				//updated area formula source: http://www.mathopenref.com/coordtrianglearea.html
				area = Math.abs(data[range_offs][0].doubleValue() * (point_a_y - avg_y) + 
						point_a_x * (avg_y - data[range_offs][1].doubleValue()) + 
						avg_x * (data[range_offs][1].doubleValue() - point_a_y)) * 0.5D;
				
				if (area > max_area) {
					max_area = area;
					max_area_point = data[range_offs];
					next_a = range_offs;
				}
				range_offs ++;
			}
			sampled[sampled_index++] = max_area_point;
			a = next_a;
		}
		
		sampled[sampled_index++] = data[data.length - 1];
		return sampled;
	}

	/* Function: upSampling
	 * input- down sampled data
	 * 		  upSampling ratio (number of raw / number of down sampled)
	 * output- up sampled data (reconstructed)
	 */
	public static Number[][] upSampling(Number[][] down_sampled, Number[] reconstructed_index) {
		Number[][] upsampled = new Number[reconstructed_index.length][];
		
		return upsampled;
	}
	
	/* Function: addSpikes additional spikes
	 * input- raw_data: x-index, y-value
	 * 		  threshold: threshold for spikes
	 * 		  reconstructed: reconstruct from down-sampled data
	 * output- selected spikes
	 */
	public static Number[][] addSpikes(Number[][] raw_data, Integer threshold) {
		Number[][] spikes = new Number[threshold][];
		return spikes;
	}
	
	/* Function: ds_spatial_decorrelation
	 * input- down sampled: x-days index, y-row, z-column
	 * 		  spikes
	 * 		  block_size
	 * output- data ready to store in database
	 */
	public static Number[][][] ds_spatial_decorrelation(Number[][][] downsampled, Number[][] spikes, Integer block_size, boolean hasSpikes) {
		/*to do*/
		
		return null;// return data which is ready to stored in database
	}
}

