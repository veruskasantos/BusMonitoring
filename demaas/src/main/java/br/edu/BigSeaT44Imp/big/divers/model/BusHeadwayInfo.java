package br.edu.BigSeaT44Imp.big.divers.model;

/**
 * 
 * @author Lucas
 *
 */
public class BusHeadwayInfo {

	private String route;
	private String busCode;
	private float headwayProgramado;
	private String busOnStopTimeStamp;

	public BusHeadwayInfo(String route, String busCode, float headwayProgramado, String busOnStopTimeStamp) {
		this.route = route;
		this.busCode = busCode;
		this.headwayProgramado = headwayProgramado;
		this.busOnStopTimeStamp = busOnStopTimeStamp;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getBusCode() {
		return busCode;
	}

	public void setBusCode(String busCode) {
		this.busCode = busCode;
	}

	public float getHeadwayProgramado() {
		return headwayProgramado;
	}

	public void setHeadwayProgramado(float headwayProgramado) {
		this.headwayProgramado = headwayProgramado;
	}

	public String getBusOnStopTimeStamp() {
		return busOnStopTimeStamp;
	}

	public void setBusOnStopTimeStamp(String busStopTime) {
		this.busOnStopTimeStamp = busStopTime;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((busCode == null) ? 0 : busCode.hashCode());
		result = prime * result + ((route == null) ? 0 : route.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BusHeadwayInfo))
			return false;
		BusHeadwayInfo other = (BusHeadwayInfo) obj;
		if (busCode == null) {
			if (other.busCode != null)
				return false;
		} else if (!busCode.equals(other.busCode))
			return false;
		if (route == null) {
			if (other.route != null)
				return false;
		} else if (!route.equals(other.route))
			return false;
		return true;
	}
	
	public float getSeconds() {
		String[] rowsTimeStamp = busOnStopTimeStamp.split(":");
		
		return Float.valueOf(rowsTimeStamp[0]) * 3600 + Float.valueOf(rowsTimeStamp[1]) * 60 + Float.valueOf(rowsTimeStamp[2]);
	}

}
