
package tk.eatheat.floatingexample;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.Random;

public class FlyBitch extends Service {

    final private String TAG = "CRAB Moving";
	private WindowManager windowManager;
	private static ImageView chatHead, chatHead2,chatHead3,chatHead4;
	private Crab_Handler uiHandler;
	private CountThread mCountThread = null;
	private static WindowManager.LayoutParams paramsF;
	private static WindowManager.LayoutParams params, params2,params3,params4;

	final int UP = 0;
	final int DOWN = 1;
	final int LEFT = 2;
	final int RIGHT = 3;
	final int LEFT_UP = 4;
	final int LEFT_DOWN = 5;
	final int RIGHT_UP = 6;
	final int RIGHT_DOWN = 7;
	final int HIDE_CRAB = 8;
	final int SMILE_CRAB = 9;


	final int ONE = 1;
	final int TWO = 2;
	final int THREE = 3;
	final int FOUR = 4;
	final int FIVE = 5;
	final int SIX = 6;
	final int SEVEN = 7;
	final int EIGHT = 8;
	final int NINE = 9;

    int display_x_size = 0;
	int display_y_size = 0;
	int cat_image_width = 0;
	int cat_image_height = 0;
	int prison_image_height = 0;
	int prison_image_width = 0;
    int Default_Current_Direction = 3; // right
	int moving_speed;
	private boolean fast_moving_enable = false;
	private boolean crab_smile_enable = false;
	private int Current_Direction = Default_Current_Direction;

	private int Before_Direction = RIGHT;
	private int current_image_num = 0;
	private boolean crab_hide_enable = false;
	private boolean crab_unhiding_enable = false;
	private int s_current_image_num = 0;


	private boolean show_menu_enabled = false;
	private boolean show_prison_enabled = false;
	private boolean show_prison_hide_menu_enabled = false;

	private int SkipTimer_Direction_change = 0;
	final private int Edge_Skip_value = 30;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		Log.d(TAG, "config changed");
		super.onConfigurationChanged(newConfig);

		int orientation = newConfig.orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
			Set_Screen_Rotation();
		else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
			Set_Screen_Rotation();
		else
			Log.w(TAG, "other: " + orientation);
	}

	private void Set_Screen_Rotation(){
		DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
		display_x_size = dm.widthPixels;
		display_y_size = dm.heightPixels;
	}

	@Override
	public void onCreate() {
		super.onCreate();

        Set_Screen_Rotation();
		Drawable b = getResources().getDrawable((R.drawable.right_crab__x1));
		cat_image_height = b.getIntrinsicHeight();
		cat_image_width = b.getIntrinsicWidth();

		Drawable pri = getResources().getDrawable((R.drawable.prison));
		prison_image_height = pri.getIntrinsicHeight();
		prison_image_width = pri.getIntrinsicWidth();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		chatHead = new ImageView(this);
		chatHead2 = new ImageView(this);
		chatHead3 = new ImageView(this);
		chatHead4 = new ImageView(this);
		chatHead.setImageResource(R.drawable.right_crab__x1);
		params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 0;
		params.y = 100;

		windowManager.addView(chatHead, params);
		paramsF = params;
		try {
			chatHead.setOnTouchListener(new View.OnTouchListener() {
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;

				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:

							// Get current time in nano seconds.

							initialX = paramsF.x;
							initialY = paramsF.y;
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();
							if(Current_Direction == UP || Current_Direction == DOWN) {
								Current_Direction = DOWN;
								fast_moving_enable = true;
							} else {
								crab_hiding();
								if(!show_menu_enabled) {
									show_menu_enabled = true;
									show_menu();
								}
							}
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:
							paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
							paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
							windowManager.updateViewLayout(chatHead, paramsF);
							break;
					}
					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}

		moving_speed = 3;

        uiHandler = new Crab_Handler();
		mCountThread = new CountThread();
		mCountThread.start();

	}

	private void show_menu() { //lock image show
		chatHead2.setImageResource(R.drawable.lock);
		params2 = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		params2.gravity = Gravity.TOP | Gravity.LEFT;
		params2.x = params.x+200;
		params2.y = params.y;
		windowManager.addView(chatHead2, params2);
		try {
			chatHead2.setOnTouchListener(new View.OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if(!show_prison_enabled) {
								show_prison_enabled =  true;
								show_presion_brick();
								hide_menu();
							} else
							{
								hide_prison_menu();
							}
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:

							break;
					}
					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void show_menu2() { //unlock image show
		chatHead4.setImageResource(R.drawable.unlock);
		params4 = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		params4.gravity = Gravity.TOP | Gravity.LEFT;
		params4.x = params3.x - 128;
		params4.y = params3.y - 128;
		windowManager.addView(chatHead4, params4);
		try {
			chatHead4.setOnTouchListener(new View.OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							hide_prison_menu();
							hide_prision_menu2();
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:

							break;
					}
					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void show_presion_brick() {
		chatHead3.setImageResource(R.drawable.prison);
		params3 = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		params3.gravity = Gravity.TOP | Gravity.LEFT;
		params3.x = params.x;
		params3.y = params.y;
		windowManager.addView(chatHead3, params3);
		try {
			chatHead3.setOnTouchListener(new View.OnTouchListener() {
						private int initialX;
						private int initialY;
						private float initialTouchX;
						private float initialTouchY;
				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							initialX = params3.x;
							initialY = params3.y;
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();
							if(show_prison_hide_menu_enabled) {
								hide_prision_menu2();
							} else
							{
								show_prison_hide_menu_enabled = true;
								show_menu2();
							}
							crab_hiding();
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:
							params3.x = initialX + (int) (event.getRawX() - initialTouchX);
							params3.y = initialY + (int) (event.getRawY() - initialTouchY);
							params.x = params3.x + 200;
							params.y = params3.y + 200;
							windowManager.updateViewLayout(chatHead3, params3);
							windowManager.updateViewLayout(chatHead, params);
							if(show_prison_hide_menu_enabled) {
								params4.x = params3.x - 128;
								params4.y = params3.y - 128;
								windowManager.updateViewLayout(chatHead4, params4);
							}
							break;
					}
					return false;
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void hide_menu() {
		windowManager.removeView(chatHead2);
		show_menu_enabled = false;
	}

	private void hide_prision_menu2() {
		windowManager.removeView(chatHead4);
		show_prison_hide_menu_enabled = false;
	}

	private void hide_prison_menu() {
		windowManager.removeView(chatHead3);
		show_prison_enabled = false;
	}

	private void crab_hiding() {
		crab_smile_enable = false;
		s_current_image_num = 0;
		crab_hide_enable = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCountThread.stopThread();
		if (chatHead != null) windowManager.removeView(chatHead);
	}

	class Crab_Handler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			current_image_num++;
			if(current_image_num > 9) {
				current_image_num = 1;
			}

			if(SkipTimer_Direction_change != 0) {
				SkipTimer_Direction_change--;
			}

			if(crab_smile_enable) {
				s_current_image_num++;
				if(s_current_image_num > 50) {
					crab_smile_enable = false;
					s_current_image_num = 0;
				}
				DISPLAY_CRAB(current_image_num, SMILE_CRAB);
			} else	if(crab_hide_enable) {
				if(crab_unhiding_enable) {
//					crab_hide_enable = false;
					if(s_current_image_num > 9) {
						s_current_image_num = 10;
					}
					s_current_image_num--;
					DISPLAY_CRAB(s_current_image_num, HIDE_CRAB);
					if(s_current_image_num == 0) {
						crab_hide_enable = false;
						crab_unhiding_enable = false;
					}

				} else {
					s_current_image_num++;
					if (s_current_image_num > 9) {
						if ((s_current_image_num / 10) == (((s_current_image_num / 10)/2)*2)) {
							DISPLAY_CRAB(8, HIDE_CRAB);
						} else {
							DISPLAY_CRAB(9, HIDE_CRAB);
						}
					} else {
						DISPLAY_CRAB(s_current_image_num, HIDE_CRAB);
					}
					if(s_current_image_num > 140) {
						crab_unhiding_enable = true;
						if(show_menu_enabled) {
							hide_menu();
						}
					}
				}
			} else {
				switch (msg.what) {
					case UP:
						run_moving_up();
						Set_CRAB_IMAGE_DIRECTION(current_image_num, UP);
						break;
					case DOWN:
						run_moving_down();
						Set_CRAB_IMAGE_DIRECTION(current_image_num, DOWN);
						break;
					case LEFT:
						run_moving_left();
						Set_CRAB_IMAGE_DIRECTION(current_image_num, LEFT);
						get_smile_random();
						break;
					case RIGHT:
						run_moving_right();
						Set_CRAB_IMAGE_DIRECTION(current_image_num, RIGHT);
						get_smile_random();
						break;
					default:
						break;
				}
			}
		}
	}

	class CountThread extends Thread implements Runnable {

		private boolean isPlay = false;

		public CountThread() {
			isPlay = true;
		}

		public void isThreadState(boolean isPlay) {
			this.isPlay = isPlay;
		}

		public void stopThread() {
			isPlay = !isPlay;
		}

		public int Get_Current_Direction(){
			return Current_Direction;
		}
		public void Set_Current_Direction(int val){
			Current_Direction = val;
		}

		@Override
		public void run() {
			super.run();

			while (isPlay) {
				Message msg = uiHandler.obtainMessage();

				switch(Current_Direction) {
					case UP:
						uiHandler.sendEmptyMessage(UP);
						break;
					case DOWN:
						uiHandler.sendEmptyMessage(DOWN);
						break;
					case LEFT:
						uiHandler.sendEmptyMessage(LEFT);
						break;
					case RIGHT:
						uiHandler.sendEmptyMessage(RIGHT);
						break;
					default:
						break;
				}

				try { Thread.sleep(110); }
				catch (InterruptedException e) { e.printStackTrace(); }

			}

		}
	}

	public void get_smile_random() {
		int new_value;
		Random r = new Random();
		new_value = r.nextInt((1000 - 0) + 0);  // 0 ~ 4
		if(new_value <= 3) {
			crab_smile_enable = true;
		}
		return;
	}


	public int get_random_value(int current)
	{
		int new_value = current;
		int moving = 1;

		while(current == new_value) {
				Random r = new Random();
				new_value = r.nextInt((4 - 0) + 0);  // 0 ~ 4
				moving = r.nextInt(10 - 1) + 1;
				if (moving > 3) {
					Set_Moving_Speed(6);
				} else {
					Set_Moving_Speed(3);
				}
			if(current == DOWN && new_value == UP) {
				new_value = current;
			}
		}
		if(SkipTimer_Direction_change != 0) {
			return current;
		}
		SkipTimer_Direction_change = Edge_Skip_value;
		return new_value;
	}

	public void Set_Moving_Speed(int val) {
		moving_speed = val;
	}
	public void run_moving_down() {

		try {
			if(fast_moving_enable) {
				paramsF.y = params.y + (1 * moving_speed + 40);
			} else {
				paramsF.y = params.y + (1 * moving_speed);
			}

			if(show_prison_enabled) {
				if(((params3.y + prison_image_height ) - 150) < params.y) {
					mCountThread.Set_Current_Direction(get_random_value(DOWN));
				} else {
					windowManager.updateViewLayout(chatHead, paramsF);
				}
			} else {
				if((display_y_size - (cat_image_height)) < params.y) {
					mCountThread.Set_Current_Direction(get_random_value(DOWN));
					if(SkipTimer_Direction_change != 0) {
						windowManager.updateViewLayout(chatHead, paramsF);
					}
				} else {
					windowManager.updateViewLayout(chatHead, paramsF);
				}
			}

		} catch (Exception ea) {
			Log.e(TAG, "Error : " + ea.toString());
		}
	}

	public void run_moving_left() {
		fast_moving_enable = false;
		try {
			paramsF.x = params.x - (1 * moving_speed);
			if(show_prison_enabled) {
				if((params3.x + 20) >= params.x) {
					mCountThread.Set_Current_Direction(get_random_value(LEFT));
				} else {
					windowManager.updateViewLayout(chatHead, paramsF);
				}
			}else {
				if(0 >= params.x) {
					mCountThread.Set_Current_Direction(get_random_value(LEFT));
					if(SkipTimer_Direction_change != 0) {
						windowManager.updateViewLayout(chatHead, paramsF);
					}
				} else {
					windowManager.updateViewLayout(chatHead, paramsF);
				}
			}
		} catch (Exception ea) {
			Log.e(TAG, "Error : " + ea.toString());
		}
	}
	public void run_moving_right() {
        fast_moving_enable = false;
		try {
			paramsF.x = params.x + (1 * moving_speed);
			if(show_prison_enabled) {
				if(((params3.x + prison_image_width) - cat_image_width - 50) < params.x) {
					mCountThread.Set_Current_Direction(get_random_value(RIGHT));
				} else {
					windowManager.updateViewLayout(chatHead, paramsF);
				}
			} else {
				if((display_x_size - (cat_image_width)) < params.x) {
					mCountThread.Set_Current_Direction(get_random_value(RIGHT));
					if(SkipTimer_Direction_change != 0)
					{
						windowManager.updateViewLayout(chatHead, paramsF);
					}
				} else {
					windowManager.updateViewLayout(chatHead, paramsF);
				}
			}


		} catch (Exception ea) {
			Log.e(TAG, "Error : " + ea.toString());
		}
	}
	public void run_moving_up() {
        fast_moving_enable = false;
		try {
			paramsF.y = params.y - (1 * moving_speed);

			if(show_prison_enabled) {
				if((params3.y + 20) >= params.y) {
					mCountThread.Set_Current_Direction(get_random_value(UP));
				} else {
					windowManager.updateViewLayout(chatHead, paramsF);
				}
			}else {
				if(0 >= params.y) {
					mCountThread.Set_Current_Direction(get_random_value(UP));
				} else {
					windowManager.updateViewLayout(chatHead, paramsF);
				}
			}


		} catch (Exception ea) {
			Log.e(TAG, "Error : " + ea.toString());
		}
	}

	public void Set_CRAB_IMAGE_DIRECTION(int mcurrent_image_num, int value) {
		switch(value) {
			case UP:
				if(Before_Direction == LEFT) {
					DISPLAY_CRAB(mcurrent_image_num,LEFT_UP);
				} else {
					DISPLAY_CRAB(mcurrent_image_num,RIGHT_UP);
				}
				break;
			case DOWN:
				if(Before_Direction == LEFT) {
					DISPLAY_CRAB(mcurrent_image_num,LEFT_DOWN);
				} else {
					DISPLAY_CRAB(mcurrent_image_num,RIGHT_DOWN);
				}
				break;
			case LEFT:
				DISPLAY_CRAB(mcurrent_image_num,LEFT);
				Before_Direction = LEFT;
				break;
			case RIGHT:
				DISPLAY_CRAB(mcurrent_image_num,RIGHT);
				Before_Direction = RIGHT;
				break;
			default:
				break;
		}

	}

	public void DISPLAY_CRAB(int imgnum, int mFRONT) {
		switch(mFRONT){
			case RIGHT:
				switch(imgnum){
					case ONE:
						chatHead.setImageResource(R.drawable.left_crab__x1);
						break;
					case TWO:
						chatHead.setImageResource(R.drawable.left_crab__x2);
						break;
					case THREE:
						chatHead.setImageResource(R.drawable.left_crab__x3);
						break;
					case FOUR:
						chatHead.setImageResource(R.drawable.left_crab__x4);
						break;
					case FIVE:
						chatHead.setImageResource(R.drawable.left_crab__x5);
						break;
					case SIX:
						chatHead.setImageResource(R.drawable.left_crab__x6);
						break;
					case SEVEN:
						chatHead.setImageResource(R.drawable.left_crab__x7);
						break;
					case EIGHT:
						chatHead.setImageResource(R.drawable.left_crab__x8);
						break;
					case NINE:
						chatHead.setImageResource(R.drawable.left_crab__x9);
						break;
					default:
						break;
				}
				break;
			case LEFT:
				switch (imgnum) {
					case ONE:
						chatHead.setImageResource(R.drawable.right_crab__x1);
						break;
					case TWO:
						chatHead.setImageResource(R.drawable.right_crab__x2);
						break;
					case THREE:
						chatHead.setImageResource(R.drawable.right_crab__x3);
						break;
					case FOUR:
						chatHead.setImageResource(R.drawable.right_crab__x4);
						break;
					case FIVE:
						chatHead.setImageResource(R.drawable.right_crab__x5);
						break;
					case SIX:
						chatHead.setImageResource(R.drawable.right_crab__x6);
						break;
					case SEVEN:
						chatHead.setImageResource(R.drawable.right_crab__x7);
						break;
					case EIGHT:
						chatHead.setImageResource(R.drawable.right_crab__x8);
						break;
					case NINE:
						chatHead.setImageResource(R.drawable.right_crab__x9);
						break;
					default:
						break;
				}
				break;
			case LEFT_DOWN:
				switch(imgnum){
					case ONE:
						chatHead.setImageResource(R.drawable.left_up_crab__x1);
						break;
					case TWO:
						chatHead.setImageResource(R.drawable.left_up_crab__x2);
						break;
					case THREE:
						chatHead.setImageResource(R.drawable.left_up_crab__x3);
						break;
					case FOUR:
						chatHead.setImageResource(R.drawable.left_up_crab__x4);
						break;
					case FIVE:
						chatHead.setImageResource(R.drawable.left_up_crab__x5);
						break;
					case SIX:
						chatHead.setImageResource(R.drawable.left_up_crab__x6);
						break;
					case SEVEN:
						chatHead.setImageResource(R.drawable.left_up_crab__x7);
						break;
					case EIGHT:
						chatHead.setImageResource(R.drawable.left_up_crab__x8);
						break;
					case NINE:
						chatHead.setImageResource(R.drawable.left_up_crab__x9);
						break;
					default:
						break;
				}
				break;
			case LEFT_UP:
				switch(imgnum){
					case ONE:
						chatHead.setImageResource(R.drawable.left_down_crab__x1);
						break;
					case TWO:
						chatHead.setImageResource(R.drawable.left_down_crab__x2);
						break;
					case THREE:
						chatHead.setImageResource(R.drawable.left_down_crab__x3);
						break;
					case FOUR:
						chatHead.setImageResource(R.drawable.left_down_crab__x4);
						break;
					case FIVE:
						chatHead.setImageResource(R.drawable.left_down_crab__x5);
						break;
					case SIX:
						chatHead.setImageResource(R.drawable.left_down_crab__x6);
						break;
					case SEVEN:
						chatHead.setImageResource(R.drawable.left_down_crab__x7);
						break;
					case EIGHT:
						chatHead.setImageResource(R.drawable.left_down_crab__x8);
						break;
					case NINE:
						chatHead.setImageResource(R.drawable.left_down_crab__x9);
						break;
					default:
						break;
				}
				break;
			case RIGHT_DOWN:
				switch(imgnum){
					case ONE:
						chatHead.setImageResource(R.drawable.right_up_crab__x1);
						break;
					case TWO:
						chatHead.setImageResource(R.drawable.right_up_crab__x2);
						break;
					case THREE:
						chatHead.setImageResource(R.drawable.right_up_crab__x3);
						break;
					case FOUR:
						chatHead.setImageResource(R.drawable.right_up_crab__x4);
						break;
					case FIVE:
						chatHead.setImageResource(R.drawable.right_up_crab__x5);
						break;
					case SIX:
						chatHead.setImageResource(R.drawable.right_up_crab__x6);
						break;
					case SEVEN:
						chatHead.setImageResource(R.drawable.right_up_crab__x7);
						break;
					case EIGHT:
						chatHead.setImageResource(R.drawable.right_up_crab__x8);
						break;
					case NINE:
						chatHead.setImageResource(R.drawable.right_up_crab__x9);
						break;
					default:
						break;
				}
				break;
			case RIGHT_UP:
				switch(imgnum){
					case ONE:
						chatHead.setImageResource(R.drawable.right_down_crab__x1);
						break;
					case TWO:
						chatHead.setImageResource(R.drawable.right_down_crab__x2);
						break;
					case THREE:
						chatHead.setImageResource(R.drawable.right_down_crab__x3);
						break;
					case FOUR:
						chatHead.setImageResource(R.drawable.right_down_crab__x4);
						break;
					case FIVE:
						chatHead.setImageResource(R.drawable.right_down_crab__x5);
						break;
					case SIX:
						chatHead.setImageResource(R.drawable.right_down_crab__x6);
						break;
					case SEVEN:
						chatHead.setImageResource(R.drawable.right_down_crab__x7);
						break;
					case EIGHT:
						chatHead.setImageResource(R.drawable.right_down_crab__x8);
						break;
					case NINE:
						chatHead.setImageResource(R.drawable.right_down_crab__x9);
						break;
					default:
						break;
				}
				break;
			case HIDE_CRAB:
				switch(imgnum){
					case ONE:
						chatHead.setImageResource(R.drawable.hide_crab__x1);
						break;
					case TWO:
						chatHead.setImageResource(R.drawable.hide_crab__x2);
						break;
					case THREE:
						chatHead.setImageResource(R.drawable.hide_crab__x3);
						break;
					case FOUR:
						chatHead.setImageResource(R.drawable.hide_crab__x4);
						break;
					case FIVE:
						chatHead.setImageResource(R.drawable.hide_crab__x5);
						break;
					case SIX:
						chatHead.setImageResource(R.drawable.hide_crab__x6);
						break;
					case SEVEN:
						chatHead.setImageResource(R.drawable.hide_crab__x7);
						break;
					case EIGHT:
						chatHead.setImageResource(R.drawable.hide_crab__x8);
						break;
					case NINE:
						chatHead.setImageResource(R.drawable.hide_crab__x9);
						break;
					default:
						break;
				}
				break;
			case SMILE_CRAB:
				if(show_prison_enabled) {
					switch (imgnum) {
						case ONE:
							chatHead.setImageResource(R.drawable.dislike_crab__x1);
							break;
						case TWO:
							chatHead.setImageResource(R.drawable.dislike_crab__x2);
							break;
						case THREE:
							chatHead.setImageResource(R.drawable.dislike_crab__x3);
							break;
						case FOUR:
							chatHead.setImageResource(R.drawable.dislike_crab__x4);
							break;
						case FIVE:
							chatHead.setImageResource(R.drawable.dislike_crab__x5);
							break;
						case SIX:
							chatHead.setImageResource(R.drawable.dislike_crab__x6);
							break;
						case SEVEN:
							chatHead.setImageResource(R.drawable.dislike_crab__x7);
							break;
						case EIGHT:
							chatHead.setImageResource(R.drawable.dislike_crab__x8);
							break;
						case NINE:
							chatHead.setImageResource(R.drawable.dislike_crab__x9);
							break;
						default:
							break;
					}
				} else {
					if (Check_Audio_playing()) {
						switch (imgnum) {
							case ONE:
								chatHead.setImageResource(R.drawable.music_crab__x1);
								break;
							case TWO:
								chatHead.setImageResource(R.drawable.music_crab__x2);
								break;
							case THREE:
								chatHead.setImageResource(R.drawable.music_crab__x3);
								break;
							case FOUR:
								chatHead.setImageResource(R.drawable.music_crab__x4);
								break;
							case FIVE:
								chatHead.setImageResource(R.drawable.music_crab__x5);
								break;
							case SIX:
								chatHead.setImageResource(R.drawable.music_crab__x6);
								break;
							case SEVEN:
								chatHead.setImageResource(R.drawable.music_crab__x7);
								break;
							case EIGHT:
								chatHead.setImageResource(R.drawable.music_crab__x8);
								break;
							case NINE:
								chatHead.setImageResource(R.drawable.music_crab__x9);
								break;
							default:
								break;
						}
					} else {
						switch (imgnum) {
							case ONE:
								chatHead.setImageResource(R.drawable.smile_crab__x1);
								break;
							case TWO:
								chatHead.setImageResource(R.drawable.smile_crab__x2);
								break;
							case THREE:
								chatHead.setImageResource(R.drawable.smile_crab__x3);
								break;
							case FOUR:
								chatHead.setImageResource(R.drawable.smile_crab__x4);
								break;
							case FIVE:
								chatHead.setImageResource(R.drawable.smile_crab__x5);
								break;
							case SIX:
								chatHead.setImageResource(R.drawable.smile_crab__x6);
								break;
							case SEVEN:
								chatHead.setImageResource(R.drawable.smile_crab__x7);
								break;
							case EIGHT:
								chatHead.setImageResource(R.drawable.smile_crab__x8);
								break;
							case NINE:
								chatHead.setImageResource(R.drawable.smile_crab__x9);
								break;
							default:
								break;
						}
					}
				}
				break;

			default:
				break;
		}
	}

	private boolean Check_Audio_playing() {
		boolean now_playing = false;

		AudioManager manager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
		if(manager.isMusicActive()) {
			now_playing = true;
		} else {
			now_playing = false;
		}
		return now_playing;
	}
}


