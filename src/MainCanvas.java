import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.Random;

import javax.swing.JPanel;

import core2d.Linha2D;
import core2d.Ponto2D;
import core2d.Bresenham;
import core3d.Mat4x4;
import core3d.ObjLoader;
import core3d.Ponto3D;
import core3d.Triangulo3D;

public class MainCanvas extends JPanel implements Runnable{
	int W = 640;
	int H = 480;
	
	Thread runner;
	boolean ativo = true;
	int paintcounter = 0;
	
	BufferedImage imageBuffer;
	byte bufferDeVideo[];
	
	Random rand = new Random();
	
	byte memoriaPlacaVideo[];
	short paleta[][];
	
	int framecount = 0;
	int fps = 0;
	
	Font f = new Font("", Font.PLAIN, 30);
	
	int clickX = 0;
	int clickY = 0;
	int mouseX = 0;
	int mouseY = 0;
	int ultimoArrastoX = 0;
	int ultimoArrastoY = 0;
	
	int pixelSize = 0;
	int Largura = 0;
	int Altura = 0;
	
	float posx = 00;
	float posy = 00;
	
	boolean LEFT = false;
	boolean RIGHT = false;
	boolean UP = false;
	boolean DOWN = false;
	boolean CAMERA_LEFT = false;
	boolean CAMERA_RIGHT = false;
	boolean CAMERA_UP = false;
	boolean CAMERA_DOWN = false;
	
	float filtroR = 1;
	float filtroG = 1;
	float filtroB = 1;
	
	float q1x = 10,q1y = 100;
	float q2x = 10,q2y = 200;
	
	//ArrayList<Linha2D> linhas = new ArrayList<Linha2D>();
	ArrayList<Triangulo3D> listaDeTriangulos = new ArrayList<>();
	
	Ponto3D p0 = null;
	Ponto3D p1 = null;
	
	Ponto2D pC = new Ponto2D(320, 240);
	
	int eixoX = 0;
	int eixoY = 0;
	
	Mat4x4 projecao;
	Mat4x4 modelview;
	
	public MainCanvas() {
		setSize(640,480);
		setFocusable(true);
		
		Largura = 640;
		Altura = 480;
		
		pixelSize = 640*480;
		imageBuffer = new BufferedImage(640,480, BufferedImage.TYPE_4BYTE_ABGR);
		
		bufferDeVideo = ((DataBufferByte)imageBuffer.getRaster().getDataBuffer()).getData();
		
		System.out.println("Buffer SIZE "+bufferDeVideo.length );
		
		
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				if (key == KeyEvent.VK_W) {
					UP = false;
				}
				if (key == KeyEvent.VK_S) {
					DOWN = false;
				}
				if (key == KeyEvent.VK_A) {
					LEFT = false;
				}
				if (key == KeyEvent.VK_D) {
					RIGHT = false;
				}
				if (key == KeyEvent.VK_I) {
					CAMERA_UP = false;
				}
				if (key == KeyEvent.VK_K) {
					CAMERA_DOWN = false;
				}
				if (key == KeyEvent.VK_J) {
					CAMERA_LEFT = false;
				}
				if (key == KeyEvent.VK_L) {
					CAMERA_RIGHT = false;
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				// System.out.println("CLICO "+key);
				if (key == KeyEvent.VK_W) {
					UP = true;
				}
				if (key == KeyEvent.VK_S) {
					DOWN = true;
				}
				if (key == KeyEvent.VK_A) {
					LEFT = true;
				}
				if (key == KeyEvent.VK_D) {
					RIGHT = true;
				}
				if (key == KeyEvent.VK_I) {
					CAMERA_UP = true;
				}
				if (key == KeyEvent.VK_K) {
					CAMERA_DOWN = true;
				}
				if (key == KeyEvent.VK_J) {
					CAMERA_LEFT = true;
				}
				if (key == KeyEvent.VK_L) {
					CAMERA_RIGHT = true;
				}
				if (key == KeyEvent.VK_Z) {
					Mat4x4 matrot = new Mat4x4();
					matrot.setSacale(0.8f, 0.8f, 0.8f);
					
					Mat4x4 mr = modelview.multiplicaMatrizes(matrot,modelview);
					modelview = mr;
				}
				if (key == KeyEvent.VK_X) {
					Mat4x4 matrot = new Mat4x4();
					matrot.setSacale(1.2f, 1.2f, 1.2f);
					
					Mat4x4 mr = modelview.multiplicaMatrizes(matrot,modelview);
					modelview = mr;
				}
				if (key == KeyEvent.VK_Q) {
					girarY(-5);
					
				}
				if (key == KeyEvent.VK_E) {
					girarY(+5);
				}
				if (key == KeyEvent.VK_1) {
					projecao.setParalelProjection();
				}
				if (key == KeyEvent.VK_2) {
					projecao.setObliqueProjection(1, 45);
				}
				if (key == KeyEvent.VK_3) {
					projecao.setObliqueProjection(0.5f, 30);
				}
				if (key == KeyEvent.VK_4) {
					projecao.setPerspectiveProjection(500, 320, 240);
				}
			}

			
		});

		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				ultimoArrastoX = 0;
				ultimoArrastoY = 0;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				clickX = e.getX();
				clickY = e.getY();
				if (e.getButton() == MouseEvent.BUTTON1) {
					ultimoArrastoX = clickX;
					ultimoArrastoY = clickY;
				}
				
				if(e.getButton()==3) {
					eixoX = clickX;
					eixoY = clickY;
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent arg0) {
				// TODO Auto-generated method stub
				mouseX = arg0.getX();
				mouseY = arg0.getY();
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				int novoX = arg0.getX();
				int novoY = arg0.getY();
				if (ultimoArrastoX != 0 || ultimoArrastoY != 0) {
					float giroY = (novoX - ultimoArrastoX) * 0.5f;
					float giroX = (novoY - ultimoArrastoY) * 0.5f;
					aplicaRotacaoOrbital(giroY, giroX);
				}
				ultimoArrastoX = novoX;
				ultimoArrastoY = novoY;
			}
		});
		
		modelview = new Mat4x4();
		modelview.setIdentity();
		System.out.println(modelview);
		
		projecao = new Mat4x4();
		projecao.setParalelProjection();

		//Triangulo3D tri = new Triangulo3D(new Ponto3D(0, 0, 0), new Ponto3D(200, 0, 0), new Ponto3D(0, 200, 0));
		carregaObjetosOBJ();
		
	}

	private void carregaObjetosOBJ() {
		listaDeTriangulos.addAll(ObjLoader.carregaPasta("obj_1"));
	}

	private void girarY(float ang) {
		Mat4x4 ida = new Mat4x4();
		ida.setTranslate(-320, -240, -50);
		Mat4x4 rot = new Mat4x4();
		rot.setRotateY(ang);
		Mat4x4 volta = new Mat4x4();
		volta.setTranslate(320, 240, 50);

		Mat4x4 m = modelview.multiplicaMatrizes(rot, ida);
		m = modelview.multiplicaMatrizes(volta, m);
		modelview = modelview.multiplicaMatrizes(m, modelview);
	}

	private void aplicaRotacaoOrbital(float giroY, float giroX) {
		Mat4x4 translacaoParaCentro = new Mat4x4();
		translacaoParaCentro.setTranslate(320, 240, 0);

		Mat4x4 translacaoParaOrigem = new Mat4x4();
		translacaoParaOrigem.setTranslate(-320, -240, 0);

		Mat4x4 rotacaoY = new Mat4x4();
		rotacaoY.setRotateY(giroY);
		Mat4x4 rotacaoX = new Mat4x4();
		rotacaoX.setRotateX(giroX);
		Mat4x4 rotacao = rotacaoY.multiplicaMatrizes(rotacaoX, rotacaoY);

		Mat4x4 centroComRotacao = translacaoParaCentro.multiplicaMatrizes(
				translacaoParaCentro, rotacao);
		Mat4x4 orbital = centroComRotacao.multiplicaMatrizes(
				centroComRotacao, translacaoParaOrigem);
		modelview = modelview.multiplicaMatrizes(orbital, modelview);
	}
	
	private void criaCubo(float x,float y, float z, float lx,float ly, float lz) {
		Ponto3D p1 = new Ponto3D(x, y, z);
		Ponto3D p2 = new Ponto3D(x+lx, y, z);
		Ponto3D p3 = new Ponto3D(x+lx, y+ly, z);
		Ponto3D p4 = new Ponto3D(x, y+ly, z);
		
		Ponto3D p5 = new Ponto3D(x, y, z+lz);
		Ponto3D p6 = new Ponto3D(x+lx, y, z+lz);
		Ponto3D p7 = new Ponto3D(x+lx, y+ly, z+lz);
		Ponto3D p8 = new Ponto3D(x, y+ly, z+lz);
		
		listaDeTriangulos.add(new Triangulo3D(p1,p2,p3));
		listaDeTriangulos.add(new Triangulo3D(p3,p4,p1));
		
		listaDeTriangulos.add(new Triangulo3D(p5,p6,p7));
		listaDeTriangulos.add(new Triangulo3D(p7,p8,p5));
		
		listaDeTriangulos.add(new Triangulo3D(p1,p4,p5));
		listaDeTriangulos.add(new Triangulo3D(p4,p8,p5));
		
		listaDeTriangulos.add(new Triangulo3D(p2,p3,p6));
		listaDeTriangulos.add(new Triangulo3D(p3,p7,p6));
		
		listaDeTriangulos.add(new Triangulo3D(p1,p2,p6));
		listaDeTriangulos.add(new Triangulo3D(p1,p6,p5));
		
		listaDeTriangulos.add(new Triangulo3D(p4,p3,p7));
		listaDeTriangulos.add(new Triangulo3D(p6,p7,p8));
	}
	
	private void criaRoda(ArrayList<Triangulo3D> listtri,float x, float y, float z) {

	    float raio = 9;

	    // Centro da roda
	    Ponto3D centro = new Ponto3D(x, y, z);

	    // Quatro pontos da roda no plano XZ
	    Ponto3D cima = new Ponto3D(
	        x,
	        y,
	        z + raio
	    );

	    Ponto3D direita = new Ponto3D(
	        x + raio,
	        y,
	        z
	    );

	    Ponto3D baixo = new Ponto3D(
	        x,
	        y,
	        z - raio
	    );

	    Ponto3D esquerda = new Ponto3D(
	        x - raio,
	        y,
	        z
	    );

	    listtri.add(
	        new Triangulo3D(centro, cima, direita)
	    );

	    listtri.add(
	        new Triangulo3D(centro, direita, baixo)
	    );

	    listtri.add(
	        new Triangulo3D(centro, baixo, esquerda)
	    );

	    listtri.add(
	        new Triangulo3D(centro, esquerda, cima)
	    );
	}
	
	private void criaCarro(ArrayList<Triangulo3D> listtri) {

	    /*
	     * ============================================================
	     * CARROCERIA
	     *
	     * X = comprimento
	     * Y = largura
	     * Z = altura
	     *
	     * 12 triângulos
	     * ============================================================
	     */

	    // Perfil esquerdo da carroceria
	    Ponto3D p1 = new Ponto3D(8, 20, 15);   // frente inferior
	    Ponto3D p2 = new Ponto3D(8, 20, 27);   // frente superior
	    Ponto3D p3 = new Ponto3D(35, 20, 31);  // capô
	    Ponto3D p4 = new Ponto3D(90, 20, 28);  // traseira superior
	    Ponto3D p5 = new Ponto3D(92, 20, 15);  // traseira inferior

	    // Perfil direito da carroceria
	    Ponto3D p6  = new Ponto3D(8, 80, 15);
	    Ponto3D p7  = new Ponto3D(8, 80, 27);
	    Ponto3D p8  = new Ponto3D(35, 80, 31);
	    Ponto3D p9  = new Ponto3D(90, 80, 28);
	    Ponto3D p10 = new Ponto3D(92, 80, 15);

	    // Lado esquerdo
	    listtri.add(new Triangulo3D(p1, p2, p3));
	    listtri.add(new Triangulo3D(p1, p3, p5));
	    listtri.add(new Triangulo3D(p3, p4, p5));

	    // Lado direito
	    listtri.add(new Triangulo3D(p6, p8, p7));
	    listtri.add(new Triangulo3D(p6, p10, p8));
	    listtri.add(new Triangulo3D(p8, p10, p9));

	    // Frente
	    listtri.add(new Triangulo3D(p1, p6, p7));
	    listtri.add(new Triangulo3D(p1, p7, p2));

	    // Traseira
	    listtri.add(new Triangulo3D(p5, p4, p9));
	    listtri.add(new Triangulo3D(p5, p9, p10));

	    // Parte inferior
	    listtri.add(new Triangulo3D(p1, p5, p10));
	    listtri.add(new Triangulo3D(p1, p10, p6));


	    /*
	     * ============================================================
	     * CABINE
	     *
	     * 12 triângulos
	     * ============================================================
	     */

	    // Perfil esquerdo
	    Ponto3D c1 = new Ponto3D(35, 20, 31);  // início
	    Ponto3D c2 = new Ponto3D(48, 20, 48);  // teto dianteiro
	    Ponto3D c3 = new Ponto3D(72, 20, 48);  // teto traseiro
	    Ponto3D c4 = new Ponto3D(84, 20, 28);  // traseira

	    // Perfil direito
	    Ponto3D c5 = new Ponto3D(35, 80, 31);
	    Ponto3D c6 = new Ponto3D(48, 80, 48);
	    Ponto3D c7 = new Ponto3D(72, 80, 48);
	    Ponto3D c8 = new Ponto3D(84, 80, 28);

	    // Lateral esquerda da cabine
	    listtri.add(new Triangulo3D(c1, c2, c3));
	    listtri.add(new Triangulo3D(c1, c3, c4));

	    // Lateral direita da cabine
	    listtri.add(new Triangulo3D(c5, c7, c6));
	    listtri.add(new Triangulo3D(c5, c8, c7));

	    // Frente da cabine
	    listtri.add(new Triangulo3D(c1, c5, c6));
	    listtri.add(new Triangulo3D(c1, c6, c2));

	    // Traseira da cabine
	    listtri.add(new Triangulo3D(c4, c3, c7));
	    listtri.add(new Triangulo3D(c4, c7, c8));

	    // Teto
	    listtri.add(new Triangulo3D(c2, c6, c7));
	    listtri.add(new Triangulo3D(c2, c7, c3));

	    // Base da cabine
	    listtri.add(new Triangulo3D(c1, c4, c8));
	    listtri.add(new Triangulo3D(c1, c8, c5));


	    /*
	     * ============================================================
	     * RODAS
	     *
	     * Cada roda = 4 triângulos
	     * 4 rodas = 16 triângulos
	     *
	     * Total:
	     * Carroceria = 12
	     * Cabine     = 12
	     * Rodas      = 16
	     * ----------------
	     * TOTAL      = 40
	     * ============================================================
	     */

	    criaRoda(listtri,25, 20, 15);
	    criaRoda(listtri,25, 80, 15);

	    criaRoda(listtri,75, 20, 15);
	    criaRoda(listtri,75, 80, 15);
	}	
	
	private void drawImageToBuffer(BufferedImage image,int x,int y, float fr, float fg, float fb) {
		byte[] imgBuffer = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		
		
		int iw = image.getWidth();
		int ih = image.getHeight();
		
		for(int yi = 0; yi < ih; yi++) {
			for(int xi = 0; xi < iw; xi++) {
				int pixi = yi*iw*4 + xi*4;
				int pixb = (yi+y)*W*4 + (xi+x)*4;
				bufferDeVideo[pixb] = imgBuffer[pixi];
			
				
				
				int b = (imgBuffer[pixi+1]&0xff);
				int g =	(imgBuffer[pixi+2]&0xff);
				int r = (imgBuffer[pixi+3]&0xff);
				
				b = (int)(b*fb);
				g = (int)(g*fg);
				r = (int)(r*fr);
//				
				b = Math.min(255, b);
				g = Math.min(255, g);
				r = Math.min(255, r);
				
				bufferDeVideo[pixb+1] = (byte)(b&0xff);
				bufferDeVideo[pixb+2] = (byte)(g&0xff);
				bufferDeVideo[pixb+3] = (byte)(r&0xff);
			}
		}
	}
	int timer = 0;
	public void simulaMundo(long diftime){
		
		float difS = diftime/1000.0f;
		float vel = 40;
		
		timer+=diftime;
		
		if(UP || DOWN || LEFT || RIGHT
				|| CAMERA_UP || CAMERA_DOWN || CAMERA_LEFT || CAMERA_RIGHT) {
			float passo = vel * difS;
			float deslocamentoX = 0;
			float deslocamentoY = 0;
			float deslocamentoZ = 0;

			if (UP) {
				deslocamentoY -= passo;
			}
			if (DOWN) {
				deslocamentoY += passo;
			}
			if (LEFT) {
				deslocamentoX -= passo;
			}
			if (RIGHT) {
				deslocamentoX += passo;
			}
			if (CAMERA_UP) {
				deslocamentoZ -= passo;
			}
			if (CAMERA_DOWN) {
				deslocamentoZ += passo;
			}
			if (CAMERA_LEFT) {
				deslocamentoX -= passo;
			}
			if (CAMERA_RIGHT) {
				deslocamentoX += passo;
			}

			Mat4x4 matrot = new Mat4x4();
			matrot.setTranslate(deslocamentoX, deslocamentoY, deslocamentoZ);
	
			Mat4x4 mr = modelview.multiplicaMatrizes(matrot,modelview);
			modelview = mr;
		}
		
		
		
//		for (int i = 0; i < listaDeTriangulos.size(); i++) {
//			Triangulo3D tri = listaDeTriangulos.get(i);
//			if (UP) {
//				tri.translacao(0, -1,0);
//			}
//			if (DOWN) {
//				tri.translacao(0, +1,0);
//			}
//			if (LEFT) {
//				tri.translacao(-1, 0,0);
//			}
//			if (RIGHT) {
//				tri.translacao(+1, 0,0);
//			}
//		}
		
	}
	
	@Override
	public void paint(Graphics g) {
		
		for(int i = 0; i < bufferDeVideo.length; i++) {
			bufferDeVideo[i] = 0;
		}
		
		
		g.setFont(f);
		
		g.setColor(Color.white);
		g.fillRect(0, 0, 800, 600);
		
		g.setColor(Color.blue);
		g.fillRect(eixoX-2, eixoY-2, 5, 5);

//		g.setColor(Color.green);
//		g.drawRect(50, 50, 700, 500);

//		g.setColor(Color.black);
//		for(int i = 0; i < listaDeLinhas.size();i++) {
//			listaDeLinhas.get(i).desenhase((Graphics2D)g);
//		}

		g.setColor(Color.black);
		for (int i = 0; i < listaDeTriangulos.size(); i++) {
			Triangulo3D tri = listaDeTriangulos.get(i);
			tri.desenhase((Graphics2D) g,modelview,projecao);
		}
		
		g.setColor(Color.red);
		if(p0!=null) {
			if(p1!=null) {
				Bresenham.desenha((Graphics2D) g, (int)p0.x, (int)p0.y,
						(int)p1.x, (int)p1.y);
				Bresenham.desenha((Graphics2D) g, (int)p0.x, (int)p0.y,
						mouseX, mouseY);
				Bresenham.desenha((Graphics2D) g, (int)p1.x, (int)p1.y,
						mouseX, mouseY);
			}else {
				Bresenham.desenha((Graphics2D) g, (int)p0.x, (int)p0.y,
						mouseX, mouseY);
			}
		}

		
		g.setColor(Color.black);
		g.drawString("FPS "+fps+" mouse: "+mouseX+","+mouseY, 10, 25);
	}
	
	public void desenhaLinhaHorizontal(int x, int y,int w) {
		int pospix = y*(W*4)+x*4;
		
		for(int i = 0; i < w;i++) {
			
			bufferDeVideo[pospix] = (byte)255;
			bufferDeVideo[pospix+1] = (byte)0;
			bufferDeVideo[pospix+2] = (byte)0;
			bufferDeVideo[pospix+3] = (byte)0;
			pospix+=4;
		}
	}
	
	public void desenhaLinhaVertical(int x, int y,int h) {
		int pospix = y*(W*4)+x*4;
		
		for(int i = 0; i < h;i++) {
			
			bufferDeVideo[pospix] = (byte)255;
			bufferDeVideo[pospix+1] = (byte)0;
			bufferDeVideo[pospix+2] = (byte)0;
			bufferDeVideo[pospix+3] = (byte)255;
			pospix+=(W*4);
		}
	}
	
	public void desenhaPixel(int x, int y,int r,int g,int b) {
		int pospix = y*(W*4)+x*4;
			
		bufferDeVideo[pospix] = (byte)255;
		bufferDeVideo[pospix+1] = (byte)(b&0xff);
		bufferDeVideo[pospix+2] = (byte)(g&0xff);
		bufferDeVideo[pospix+3] = (byte)(r&0xff);
	
	}
	
	public void start(){
		runner = new Thread(this);
		runner.start();
	}
	

	
	
	@Override
	public void run() {
		long time = System.currentTimeMillis();
		long segundo = time/1000;
		long diftime = 0;
		while(ativo){
			simulaMundo(diftime);
			paintImmediately(0, 0, 640, 480);
			paintcounter+=100;
			
			try {
				Thread.sleep(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long newtime = System.currentTimeMillis();
			long novoSegundo = newtime/1000;
			diftime = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			framecount++;
			if(novoSegundo!=segundo) {	
				fps = framecount;
				framecount = 0;
				segundo = novoSegundo;
			}
		}
	}
	
}
