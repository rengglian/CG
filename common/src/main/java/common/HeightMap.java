package common;

public class HeightMap {

	private static float[][] m_heightMap;
	private static int m_mapSize;
	private static float m_roughness;
	private static float m_decay;
	private static float m_min;
	private static float m_max;
	private static float m_mean;
	private static boolean DEBUG = false;
	
	private static void DEBUG_PRINTF()
	{
		for(int m = m_mapSize-1; m >=0; m--)
		{
			for(int n = m_mapSize-1;n >=0; n--)
			{
				System.out.printf("%.3f ", m_heightMap[m][n]);
			}
			System.out.printf("\n");
		}		
		System.out.printf("\n");
	}
	
	private static float rand(float roughness)
	{
		return (float)(2.0*Math.random()-1.0)*roughness;
	}
	
	private static void seed()
	{

		m_heightMap[0][0] 						= rand(m_roughness);
		m_heightMap[0][m_mapSize-1] 			= rand(m_roughness);
		m_heightMap[m_mapSize-1][0] 			= rand(m_roughness);
		m_heightMap[m_mapSize-1][m_mapSize-1]	= rand(m_roughness);

		if(DEBUG)DEBUG_PRINTF();
		float roughness = m_roughness;
		int matrixSize = m_mapSize;
		for (int i = 0; i < Math.sqrt(m_mapSize); i++)
		{
		    int delta = matrixSize >> 1;
		    for (int j = 0; j < m_mapSize-1; j += (matrixSize-1))
		    {
		    	for (int k = 0; k < m_mapSize-1; k += (matrixSize-1))
		    	{
		    		avgSquare(j, k, matrixSize, roughness);
		    		if(DEBUG)DEBUG_PRINTF();
		    	}
		    }
		    if (delta > 0)
		    {
		    	for (int j = 0; j <= m_mapSize-1; j += delta)
		    	{
		    		for (int k = (j + delta) % (matrixSize-1) ; k <= m_mapSize-1; k += matrixSize-1)
		    		{
		    			avgDiamand((j-delta), (k-delta), matrixSize, roughness);
		    		    if(DEBUG)DEBUG_PRINTF();
		    		}
		    	}
		    }
	    	roughness /= m_decay;
			matrixSize = matrixSize/2 + 1;
		}
		

		m_min = m_max = m_heightMap[0][0];
		double mean = 0.0;
		for(int i = 1; i <m_mapSize; i++)
		{
			for(int j = 1; j< m_mapSize; j++)
			{
				if(m_min > m_heightMap[i][j]) m_min = m_heightMap[i][j];
				if(m_max < m_heightMap[i][j]) m_max = m_heightMap[i][j];
				mean += m_heightMap[i][j];
			}
		}
		m_mean = (float)(mean/(m_mapSize*m_mapSize));
	}
	
	private static void avgSquare (int x, int y, int side, float roughness) 
	{
		if (side > 1)
		{
			int half = side / 2;
			float avg = (m_heightMap[x][y] + m_heightMap[x + side -1 ][y] +
					m_heightMap[x + side -1][y + side - 1] + m_heightMap[x][y + side - 1]) * 0.25f;
			m_heightMap[x + half][y + half] = (avg + rand(roughness));
	    }
	}

	private static void avgDiamand (int x, int y, int side, float roughness) 
	{
		int half = side / 2;
		float avg = 0.0f; 
		float nbrOfNodes = 0.0f;
		if(x >= 0)
		{
			avg += m_heightMap[x][y + half];
			nbrOfNodes += 1.0;
		}
		if(y >= 0)
		{
			avg += m_heightMap[x + half][y];
			nbrOfNodes += 1.0;
		}
		if(x + side -1 < m_heightMap.length)
		{
			avg += m_heightMap[x + side -1][y + half];
			nbrOfNodes += 1.0; 
		}
		if(y + side -1 < m_heightMap.length)
		{
			avg += m_heightMap[x + half][y + side -1];
			nbrOfNodes += 1.0;
		}
		m_heightMap[x + half][y + half] = (avg / nbrOfNodes + rand(roughness));
	}
	
	
	public HeightMap(int n, float roughness, float decay)
	{
		if(DEBUG)n = 2;
		m_mapSize = ((int)Math.pow(2, n)+1);
		m_heightMap = new float[m_mapSize][m_mapSize];
		m_roughness = roughness;
		m_decay = decay;
		seed();
	}	
	
	public float getHeight(int x, int y)
	{
		return m_heightMap[x][y];
	}
	
	public float getMin()
	{
		return m_min;
	}
	public float getMax()
	{
		return m_max;
	}
	public float getMean()
	{
		return m_mean;
	}
	public int getDimension()
	{
		return m_mapSize;
	}
	public float getDecay()
	{
		return m_decay;
	}
}
