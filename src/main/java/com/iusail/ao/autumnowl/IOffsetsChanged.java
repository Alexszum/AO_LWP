package com.iusail.ao.autumnowl;
//связано с паралаксом 
public interface IOffsetsChanged {
	
	public void offsetsChanged(float xOffset, float yOffset,
							   float xOffsetStep, float yOffsetStep, int xPixelOffset,
							   int yPixelOffset);
}
