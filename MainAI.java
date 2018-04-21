package RaceAI.AI;

import java.awt.Point;
import java.util.Random;
import java.util.Vector;

import RaceAI.RaceClient.Car;
import RaceAI.RaceClient.Race;
import RaceAI.AI.Matrix;

public class MainAI {
	Race race;
	Vector<Car> All_cars;
	Car Mycar;
	Matrix[][] matrix= new Matrix[1000][1000];
	
	public String key = "0000"; // Go-Back-Left-Right (Up - Down - Left - Right)
	
	public MainAI(Race race, Vector<Car> cars, Car Mycar){
		this.race = race;
		this.Mycar = Mycar;
		this.All_cars = cars;
		System.out.println("Ban do co Row = 0"+this.race.BlockRow()+" va Column = "+this.race.BlockColumn());
		
		longRow= this.race.BlockRow()-2;
		longCol= this.race.BlockColumn()-2;
		finish= new Point(longCol, longRow);
		maxPercent= Math.sqrt( longRow*longRow + longCol*longCol );
		readyMatrix();
		numCar= All_cars.size();
		System.out.println("Co tong cong "+numCar+" nguoi choi!");
		blockSize= this.race.BlockSize();
	}

	// your variants
	Point now= new Point(1, 1), finish, next= now, difNow= new Point(0, 0);
	Point last=new Point(0, 0);
	Random rand = new Random();
	int[] ix = {0, 1, 0, -1, -1, -1, 1,  1};
	int[] iy = {1, 0, -1, 0, -1,  1, 1, -1};
	int gDown= 0, gRight= 1, gUp= 2, gLeft= 3;
	float[] percent = {0, 0, 0, 0};
//					2 la di len
//	3 la qua trai					1 la qua phai
//					0 la di xuong
	
	//last position
	double lastX=0,lastY=0;
	// last speed
	double speed = 0;
	
/// Write your AI here ...

	int maxMapSize= 1000;
	int numCar= 1;
	int blockSize= 5;
	
	//	Break car stuck
	int carStuck= 0;
	boolean turnCar= false;
	void runStuck()
	{
		if( carStuck%150 == 0 )
		{
			System.out.print("I had try move "+carStuck+" time! ");
			System.out.print("Because speed is "+( speed < 10E-58 && ( this.key!="0010" && this.key != "0001") ) );
			System.out.println(" or "+(carStuck%20 != 0)+" or "+(speed < 10E-70)+"   "+finish.x+" "+finish.y );
		}
		
		carStuck++;
		if( carStuck/150<5 )
			switch( (carStuck%150)/50 )
			{
				case 0:						//	go back
					this.key = "0110";
					return;
				case 1:						//	go back
					this.key = "0101";
					return;
				case 2:
					this.key = "1001";
					return;
//			case 2:						//	go ahead
//				if( goBack == 1 )			//	anh left
//					this.key = "1010";
//				else 						//	 and right
//					this.key = "1001";
//				return;
//			case 3:						//	go ahead
//				if( goBack == 1 )			//	anh left
//					this.key = "1000";
//				else 						//	 and right
//					this.key = "1000";
//				return;
//			default:
//				switch ( rand.nextInt(4) )
//				{
//					case 0: this.key = "0110";	//	go back and left
//							return;
//					case 1: this.key = "0101";	//	go back and right
//							return;
//					case 2: this.key = "1010";	//	go ahead and left
//							return;
//					case 3: this.key = "1001";	//	go ahead and right
//							return;
//				}
			}
			else
			{
				if( rand.nextInt(2) == 1)
				{
					this.key= "1001";
					return;
				}
				else
				{
					this.key= "1010";
					return;
				}
			}
		}
	
	void resetMap()
	{
		int lengRow= this.race.BlockRow();
		int lengCol= this.race.BlockColumn();
		for(int r= 1; r < lengRow-1; r++)
			for(int c= 1; c < lengCol-1; c++)
			{
				if( matrix[c][r].map() != '1' && matrix[c][r].map() != ' ')
					matrix[c][r].upPercent( percent(c,r) );
			}
		iCanCheckAllMap();
		iGuestWay2Finish();	
	}
	
	void checkResetMap()
	{
		Point noWhere= new Point( 0, 0);
		Point nextNow= findNext(now.x, now.y, noWhere);
		if( samePoint(nextNow, noWhere) )
			resetMap();
	}
	
	
	
	
	
	
	//	Break car crack
	int carCrack= 0;
	int countCarCrack= 0;
	
	//	Print out to test
	int cout= 0;
	
///	Point
	boolean samePoint(Point a, Point b)
	{
		if( a.x == b.x && a.y == b.y )
			return true;
		else
			return false;
	}

	boolean samePoint(Point a, int x, int y)
	{
		if( a.x == x && a.y == y )
			return true;
		else
			return false;
	}

	
///	Matrix
	
	boolean finding= false;
	boolean turn= false;

	int longRow= 0;
	int longCol= 0;
	double maxPercent= 1;
	
	boolean endRoad= false;

	char[][] matrixMove= new char[maxMapSize][maxMapSize];

	float percent(int x, int y)
	{
		float percentCol= (finish.x - x);
		float percentRow= (finish.y - y);
		
		return (1- (float) (Math.sqrt(percentCol*percentCol + percentRow*percentRow)/maxPercent));
	}
	

	void readyMatrix()
	{
		int lengRow= this.race.BlockRow();
		int lengCol= this.race.BlockColumn();
		for(int r= 0; r < lengRow; r++)
			for(int c= 0; c < lengCol; c++)
			{
				if( c > 0 && r > 0 && c < lengCol-1 && r < lengRow-1 )		//	Phan o giua
					matrix[c][r]= new Matrix(' ', percent(c, r), 0);
				else														//	Phan ngoai ria
					matrix[c][r]= new Matrix('1', -1, 0);
			}
		matrix[1][1].upMap('0');
	}

	int countWall(int x, int y)
	{
		int countWall= 0;
		for(int i= 0; i < 4; i++)
			if( matrix[x+ix[i]][y+iy[i]].map() == '1' || matrix[x+ix[i]][y+iy[i]].percent() == -1 )
				countWall++;
		return countWall;
	}
	
	void setFirst(int x, int y)
	{
		for(int i= 0; i < 8; i++)
			if( matrix[x+ix[i]][y+iy[i]].map() == ' ' )
			{
				matrix[x+ix[i]][y+iy[i]].upMap( this.race.BlockKind(x+ix[i], y+iy[i]) );
				if( matrix[x+ix[i]][y+iy[i]].map() == '1' )
				{
					matrix[x+ix[i]][y+iy[i]].upPercent(-1);				//	Tuong thi khong co kha nang ve dich
				}
			}
		iCanCheckAllMap();
		iGuestWay2Finish();	
		setAgain( x, y);
	}
	
	void setAgain(int c, int r)
	{
		matrix[c][r].upDir(4-countWall(c, r) );
		matrix[c][r].upPass();
		if( matrix[c][r].dir() <= 1 && !samePoint(finish, c, r)) matrix[c][r].upPercent(-1);//	Duong cut
		this.next = findNext(c, r, this.last);
		this.last = this.now;
		
		finding= ( matrix[next.x][next.y].pass == 0 );
		if( !finding )
		{
			turn= matrix[now.x][now.y] != matrix[next.x][next.y];
		}	
	}
	
	void setMatrix(int c, int r)
	{
		if( matrix[c][r].pass() == 0 )
			setFirst(c, r);
		else
			setAgain(c, r);
	}

	void printMatrix()
	{
		for(int r= 0; r < this.race.BlockRow(); r++)
		{
			for(int c= 0; c < this.race.BlockColumn(); c++)
				if( matrix[c][r].map() == ' ' )
					System.out.printf( "%2c",' ' );
				else if( matrix[c][r].map() == '1' )
					System.out.printf( "%2c",'.' );
				else if( matrix[c][r].percent() == -1 )
					System.out.printf( "%2c",'+' );					
				else
					System.out.printf( "%2.0f",matrix[c][r].percent()*10);
			System.out.println();
		}
	}
	
	Point next1Way(int x, int y, Point last)
	{
		for(int i= 0; i < 4; i++)
			if ((last.x!=x+ix[i] || last.y!=y+iy[i]) && matrix[x+ix[i]][y+iy[i]].percent() > -0.1 )
				return new Point(x+ix[i], y+iy[i]);
		return last;
	}
	
	Point nextSomeWay(int x, int y, Point last)
	{
		int minPass= 5;
		for(int i= 0; i < 4; i++)
			if ((last.x!=x+ix[i] || last.y!=y+iy[i]) && matrix[x+ix[i]][y+iy[i]].percent() > -0.1 )
			{
				if( minPass > matrix[x+ix[i]][y+iy[i]].pass() )
					minPass= matrix[x+ix[i]][y+iy[i]].pass();
			}
		
		int iNext= 0;
		boolean setFirst= true;
		for(int i= 0; i < 4; i++)
			if ( !samePoint(last, x, y) && matrix[x+ix[i]][y+iy[i]].percent()+percent[i] > -0.1 )
			{
				if(( matrix[x+ix[iNext]][y+iy[iNext]].percent()+percent[iNext] < matrix[x+ix[i]][y+iy[i]].percent()+percent[i]
					|| setFirst ) && matrix[x+ix[i]][y+iy[i]].pass() == minPass )
				{
					iNext= i;
					setFirst= false;
				}
			}
		if( !setFirst )
			return new Point(x+ix[iNext], y+iy[iNext]);
		else
			return last;
	}
	
	Point findNext(int x, int y, Point last)
	{
		if( matrix[x][y].dir() == 1 )
			return next1Way(x, y, last);
		else
			return nextSomeWay(x, y, last);
	}

	void iCanGuest(int x, int y)
	{
		for( int i= 0; i < 4; i++)
			if( matrix[x + ix[i]][y + iy[i]].map() != '1' && matrix[x+ix[i]][y+iy[i]].pass() == 0 )
			{
				matrix[x + ix[i]][y + iy[i]].upMap('1');
				matrix[x + ix[i]][y + iy[i]].upPercent( -1 );
				iCanGuest(x + ix[i], y + iy[i]);
			}
	}

	boolean checkFinish= true;
	void iCheckHere(int x, int y)
	{
		if( matrix[x][y].dir() > 0 )
		if( countWall(x, y) == 3 && !samePoint(this.next, x, y) )				//	Tranh khoa xe thanh 1 cuc
		{
			if( !samePoint(finish, x, y) ) 										//	Tranh khoa luon cai dich cai khoi ve :D
			{
//				System.out.println("now is [ "+x+" : "+y+" ] ");
				matrix[x][y].upMap('0');
				matrix[x][y].upPercent(-1);
	
				Point nextCheck= next1Way(x, y, new Point(x, y));
				iCheckHere( nextCheck.x, nextCheck.y );
			}
			else if( checkFinish )												//	Chi check 1 lan la du
			{
				checkFinish= false;
/*				Point nextCheck= next1Way(x, y, new Point(x, y));
				if( nextCheck.x == this.race.BlockColumn()-3 )					//	Duong ve dich chi con trong ben trai :D
				{
					percent[gDown]= 0.45f;
					percent[gLeft]= 0.30f;
					percent[gUp]  = 0.15f;
				}
				else
				{
					percent[gRight]= 0.45f;
					percent[gUp]   = 0.30f;
					percent[gLeft] = 0.15f;
				}*/
			}
		}
	}
	
	void iCanCheckAllMap()
	{
		for(int y= 0; y < this.race.BlockRow()/2; y++)
			for(int x= 0; x < this.race.BlockColumn()/2; x++)
				if( countWall( 2*x+1, 2*y+1) == 3 )
					iCheckHere(2*x+1, 2*y+1);
	}

	void checkWay2Finish(int x, int y, boolean check)
	{
		for(int i= 0; i < 4; i++)
			if( matrix[x + ix[i]][y + iy[i]].guest() == check && matrix[x + ix[i]][y + iy[i]].map() == ' ' )
			{
				matrix[x + ix[i]][y + iy[i]].upGuest();
				checkWay2Finish(x+ix[i], y+iy[i], check);
			}
	}
	
	void blockWay2Finish()
	{
		for(int r= 1; r < this.race.BlockRow()-1; r++)
			for(int c= 1; c < this.race.BlockColumn()-1; c++)
				if( matrix[c][r].map() == ' ' && matrix[c][r].guest() == false )
				{
					matrix[c][r].upMap('0');
					matrix[c][r].upPercent(-1);
				}
	}
	
	void iGuestWay2Finish()								//	Try to find what block can't go to finish
	{
		checkWay2Finish(finish.x, finish.y, false);			//	Check
		blockWay2Finish();
		checkWay2Finish(finish.x, finish.y, true);			//	Uncheck
	}

	void checkOtherCar()
	{
		for(int i= 0; i < numCar; i++)
		{
			int x= (int)All_cars.elementAt(i).getx()/blockSize;
			int y= (int)All_cars.elementAt(i).gety()/blockSize;
			if( matrix[x][y].map() == ' ' )	matrix[x][y].upMap('0');
		}
	}
	
	public void AI()
	{
		cout++;
		
		checkOtherCar();
		double xCar= this.Mycar.getx();
		double yCar= this.Mycar.gety();
		
		int x = (int) (xCar / this.race.BlockSize());
		int y = (int) (yCar / this.race.BlockSize());
		this.now = new Point(x,y);
		
		if( carStuck%150 != 0  )
		{
			if( !samePoint(now, difNow) && matrix[now.x][now.y].percent() == -1 )
			{
				if( matrix[difNow.x][difNow.y].percent() == -1 )
					matrix[difNow.x][difNow.y].upPercent(0);
				difNow= now;
			}
			runStuck();
			return;
		}

		if( x>0 && y>0 && !samePoint(this.now, this.difNow) )	
		{
			difNow= now;
			setMatrix(x, y);
		}
		
		double speed_now = Math.sqrt((xCar-lastX)*(xCar-lastX)+(yCar-lastY)*(yCar-lastY));
		speed = (speed*2+speed_now)/3;
		lastX=xCar;
		lastY=yCar;
		
		if( cout%1000 == 0 ) 
		{
			printMatrix();
			System.out.print(speed+ ", ["+x+" : "+y+"]    "+this.race.BlockKind(x, y));
			System.out.printf("v%1.2f  >%1.2f  ^%1.2f  <%1.2f\n", percent[0], percent[1], percent[2], percent[3]);
			System.out.println("-----------------------------------------------------------");
		}
		
		//	Slow down my car
		if( ( speed > 2.8 && ( finding || turn ) ) || ( speed > 3.5 ) )
		{
			key= "0100";
			return;
		}
		
		// Break car stuck
		
		if ( ( speed < 10E-58 && turnCar == false ) || speed < 10E-70 )
		{
			if( !samePoint(last, 1, 1) )
			{
				checkResetMap();
				runStuck();
				return;
			}
		}
		else 
		{
			if( carStuck/150 >= 5 )	carStuck= 0;
		}
		

		
		//	Next Block Center Coordinate
		double block_center_x= (this.next.x + 0.5) * this.race.BlockSize();
		double block_center_y= (this.next.y + 0.5) * this.race.BlockSize();
		
		//	Car's Direction
		double v_x = Math.cos(this.Mycar.getalpha() * Math.PI/180);
		double v_y = Math.sin(this.Mycar.getalpha() * Math.PI/180);
		
		//	Vector to Next Block Center from Car's position
		double c_x = block_center_x - xCar;
		double c_y = block_center_y - yCar;
		double distance2center = Math.sqrt(c_x*c_x+c_y*c_y);
		if (distance2center!=0) {
			//vector normalization
			c_x/=distance2center;
			c_y/=distance2center;
		}
		
		
		if (distance2center<this.race.BlockSize()*0.49){
//			this.key = "0000"; //stop
			// find new next block

		}
		else {
			// Go to next block center
			double inner = v_x*c_x + v_y*c_y;
			double outer = v_x*c_y - v_y*c_x;
			if (inner > 0.995){
				turnCar= false;
				this.key = "1000"; //go
			} else {
				turnCar= true;
				if (inner < 0){
					this.key = "0001"; //turn right
				}
				else {
					if (this.race.BlockKind(x, y)!='3')
						if (outer > 0) this.key = "0001"; //turn right
						else this.key = "0010"; //turn left
					else 
						if (outer > 0) this.key = "0010"; //turn right
						else this.key = "0001"; //turn left
				}
			}
		}
	}

}
