
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
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
	private static ImageView main_crab_ani, lock_view,prison_brick,unlock_view,canned_food,shrimp_food,lotto_ball;
//	private AnimationDrawable main_crab_ani;
	static private Crab_Handler uiHandler;
	private CountThread mCountThread = null;
	private static WindowManager.LayoutParams paramsF;
	private static WindowManager.LayoutParams params, lock_view_params,prison_brick_params,unlock_view_params,canned_food_params,shrimp_food_params,lotto_ball_params;

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
	final int crab_hide_duration = 80;

	final int ONE = 1;
	final int TWO = 2;
	final int THREE = 3;
	final int FOUR = 4;
	final int FIVE = 5;
	final int SIX = 6;
	final int SEVEN = 7;
	final int EIGHT = 8;
	final int NINE = 9;
    final int POSITION_X = 0;
	final int POSITION_Y = 1;

    int display_x_size = 0;
	int display_y_size = 0;
	int crab_size_width = 0;
	int crab_size_height = 0;
	int prison_image_height = 0;
	int prison_image_width = 0;
	int shrimp_food_size_x = 0;
	int shrimp_food_size_y = 0;
	int food_position[] = {0,0};

    int Default_Current_Direction = 3; // right
	int moving_speed;
	int current_phone_state = 0;
	final int PHONE_IDLE = 0;
	final int PHONE_HOOK = 1;
	final int PHONE_RING = 2;
	private boolean fast_moving_enable = false;
	private boolean crab_smile_enable = false;
	private int Current_Direction = Default_Current_Direction;
	private boolean Direction_Changed = false;
	private int fast_duration = 0;
	private int default_thread_speed = 100;
	private int thread_speed = default_thread_speed;

	private int Before_Direction = RIGHT;
	private int current_image_num = 0;
	private boolean crab_hide_enable = false;
	private boolean crab_unhiding_enable = false;
	private int s_current_image_num = 0;
	private TelephonyManager mTelMan;


	private boolean show_menu_enabled = false;
	private boolean show_shrimp_enabled = false;
	private boolean show_lotto_ball_enabled = false;
	private boolean show_prison_enabled = false;
	private boolean show_prison_hide_menu_enabled = false;

	final private int first_menu_poinster = 130;
	final private int second_menu_poinster = 260;
	final private int third_menu_poinster = 390;

	private int SkipTimer_Direction_change = 0;
	final private int Edge_Skip_value = 1;
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
	public void onCreate( ) {
		super.onCreate();

        Set_Screen_Rotation();
		Drawable b = getResources().getDrawable((R.drawable.right_crab__x1));
		crab_size_height = b.getIntrinsicHeight();
		crab_size_width = b.getIntrinsicWidth();

		Drawable pri = getResources().getDrawable((R.drawable.prison2));
		prison_image_height = pri.getIntrinsicHeight();
		prison_image_width = pri.getIntrinsicWidth();

		Drawable shrimp_food_size =  getResources().getDrawable((R.drawable.shrimp_food));
		shrimp_food_size_x = shrimp_food_size.getIntrinsicWidth();
		shrimp_food_size_y = shrimp_food_size.getIntrinsicHeight();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

		main_crab_ani = new ImageView(this);
		lock_view = new ImageView(this);
		prison_brick = new ImageView(this);
		unlock_view = new ImageView(this);
		canned_food = new ImageView(this);
		shrimp_food = new ImageView(this);
		lotto_ball = new ImageView(this);

		main_crab_ani.setImageResource(R.drawable.right_crab__x1);
//		main_crab_ani.setBackgroundResource(R.drawable.left_move_90);
//		main_crab_ani = (AnimationDrawable) main_crab_ani.getBackground();
//		main_crab_ani.start();
		params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.START;
		params.x = 0;
		params.y = 100;

		windowManager.addView(main_crab_ani, params);
		paramsF = params;

		mTelMan = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelMan.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		try {
			main_crab_ani.setOnTouchListener(new View.OnTouchListener() {
				private int initialX;
				private int initialY;
				private float initialTouchX;
				private float initialTouchY;

				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:

							initialX = paramsF.x;
							initialY = paramsF.y;
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();

							if(Current_Direction == UP || Current_Direction == DOWN) {
								Current_Direction = DOWN;
								fast_moving_enable = true;
								Set_Moving_Speed(10);
								fast_duration = 200;
								Set_Thread_Speed(default_thread_speed / 3);
							} else {
								if(get_fast_or_hide()) {
									fast_moving_enable = true;
									Set_Moving_Speed(8);
									fast_duration = 50;
									Set_Thread_Speed(default_thread_speed / 3);
								} else {
									if(!crab_hide_enable) crab_hiding();
									if (!show_menu_enabled) {
										show_menu_enabled = true;
										show_menu();
									}
								}
							}
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:
							paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
							paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
							windowManager.updateViewLayout(main_crab_ani, paramsF);
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
		mCountThread.setName("mCountThread");
		mCountThread.start();

	}

	PhoneStateListener phoneStateListener = new PhoneStateListener()
	{
		public void onCallStateChanged (int state, String incomingNumber) {
			switch (mTelMan.getCallState()) {
				case TelephonyManager.CALL_STATE_IDLE:
					Set_Phone_status(PHONE_IDLE);
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					Set_Phone_status(PHONE_RING);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					Set_Phone_status(PHONE_HOOK);
					break;
			}
		}
	};

	private void Set_Phone_status (int stat) {
		current_phone_state = stat;
	}
	private int Get_Phone_status() {
		return current_phone_state;
	}

	private void show_food_menu() {

		canned_food.setImageResource(R.drawable.canned_food);
		canned_food_params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		canned_food_params.gravity = Gravity.TOP | Gravity.START;
			if (params.x > (display_x_size / 3)) {
				canned_food_params.x = params.x - second_menu_poinster;
			} else {
				canned_food_params.x = params.x + second_menu_poinster;
			}

		canned_food_params.y = params.y;
		windowManager.addView(canned_food, canned_food_params);
		try {
			canned_food.setOnTouchListener(new View.OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if(!show_shrimp_enabled) {
								show_shrimp_enabled = true;
								show_shrimp_twins();
								if(!show_prison_enabled){
									hide_menu();
								}
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

	private void show_lotto_menu() {
		lotto_ball.setImageResource(R.drawable.lotto_ball);
		lotto_ball_params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		lotto_ball_params.gravity = Gravity.TOP | Gravity.START;
		if(params.x > (display_x_size / 3)) {
			lotto_ball_params.x = params.x - third_menu_poinster;
		} else {
			lotto_ball_params.x = params.x + third_menu_poinster;
		}
		lotto_ball_params.y = params.y;
		windowManager.addView(lotto_ball, lotto_ball_params);
		try {
			lotto_ball.setOnTouchListener(new View.OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if(!show_lotto_ball_enabled) {
								show_lotto_ball_enabled =  true;

								DO_LOTTO_RANDOM();
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

	private void show_lock_menu() {
		lock_view.setImageResource(R.drawable.lock);
		lock_view_params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		lock_view_params.gravity = Gravity.TOP | Gravity.START;
		if(params.x > (display_x_size / 3)) {
			lock_view_params.x = params.x - first_menu_poinster;
		} else {
			lock_view_params.x = params.x + first_menu_poinster;
		}
		lock_view_params.y = params.y;
		windowManager.addView(lock_view, lock_view_params);
		try {
			lock_view.setOnTouchListener(new View.OnTouchListener() {
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

	private void show_menu() { //lock image show
        show_lotto_menu();
		show_food_menu();
		show_lock_menu();
	}

	private void show_unlock_menu() { //unlock image show
		unlock_view.setImageResource(R.drawable.unlock);
		unlock_view_params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		unlock_view_params.gravity = Gravity.TOP | Gravity.START;
		windowManager.addView(unlock_view, unlock_view_params);
			if(prison_brick_params.x > (display_x_size / 3)) {
				unlock_view_params.x = prison_brick_params.x - 130;
			}else {
				unlock_view_params.x = prison_brick_params.x + prison_image_width;
			}
			unlock_view_params.y = prison_brick_params.y;
			windowManager.updateViewLayout(unlock_view, unlock_view_params);

		try {
			unlock_view.setOnTouchListener(new View.OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							hide_prison_menu();
							hide_prision_menu2();
							hide_food_menu();
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
//	shrimp_food.setBackgroundResource(R.drawable.right_move);
//	shrimp_food_ani = (AnimationDrawable) shrimp_food.getBackground();
//	shrimp_food_ani.start();
	private void show_shrimp_twins() { //unlock image show
		shrimp_food.setImageResource(R.drawable.shrimp_food);
		shrimp_food_params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		shrimp_food_params.gravity = Gravity.TOP | Gravity.START;
		if(show_prison_enabled) {
			shrimp_food_params.x = prison_brick_params.x + get_random_locate(prison_image_width, shrimp_food_size_x);
			shrimp_food_params.y = prison_brick_params.y + get_random_locate(prison_image_height, shrimp_food_size_y);
		} else {
			shrimp_food_params.x = get_random_locate(display_x_size, shrimp_food_size_x);
			shrimp_food_params.y = get_random_locate(display_y_size, shrimp_food_size_y);
		}
		Set_Food_Position(POSITION_X, shrimp_food_params.x);
		Set_Food_Position(POSITION_Y, shrimp_food_params.y);
		windowManager.addView(shrimp_food, shrimp_food_params);

		try {
			shrimp_food.setOnTouchListener(new View.OnTouchListener() {
				private int food_initialX;
				private int food_initialY;
				private float food_initialTouchX;
				private float food_initialTouchY;
				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							food_initialX = shrimp_food_params.x;
							food_initialY = shrimp_food_params.y;
							food_initialTouchX = event.getRawX();
							food_initialTouchY = event.getRawY();
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:
							shrimp_food_params.x = food_initialX + (int) (event.getRawX() - food_initialTouchX);
							Set_Food_Position(POSITION_X,shrimp_food_params.x);
							shrimp_food_params.y = food_initialY + (int) (event.getRawY() - food_initialTouchY);
							Set_Food_Position(POSITION_Y,shrimp_food_params.y);
							windowManager.updateViewLayout(shrimp_food, shrimp_food_params);
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
		prison_brick.setImageResource(R.drawable.prison2);
		prison_brick_params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);
		prison_brick_params.gravity = Gravity.TOP | Gravity.START;
		prison_brick_params.x = params.x;
		prison_brick_params.y = params.y;
		windowManager.addView(prison_brick, prison_brick_params);
		try {
			prison_brick.setOnTouchListener(new View.OnTouchListener() {
						private int initialX;
						private int initialY;
						private float initialTouchX;
						private float initialTouchY;
				@Override public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							initialX = prison_brick_params.x;
							initialY = prison_brick_params.y;
							initialTouchX = event.getRawX();
							initialTouchY = event.getRawY();
							if (show_prison_hide_menu_enabled) {
								hide_prision_menu2();
								hide_food_menu();
							} else {
								show_prison_hide_menu_enabled = true;
								show_unlock_menu();
								show_food_menu();
							}
							if(!crab_hide_enable) crab_hiding();
							break;
						case MotionEvent.ACTION_UP:
							break;
						case MotionEvent.ACTION_MOVE:
							prison_brick_params.x = initialX + (int) (event.getRawX() - initialTouchX);
							prison_brick_params.y = initialY + (int) (event.getRawY() - initialTouchY);
							params.x = prison_brick_params.x + (prison_image_width / 3);
							params.y = prison_brick_params.y + (prison_image_height / 7);

							if (prison_brick_params.y < 0) {
								prison_brick_params.y = 0;
							}
							if (prison_brick_params.y > (display_y_size - prison_image_height)) {
								prison_brick_params.y = display_y_size - prison_image_height;
							}

							if (prison_brick_params.x < 0) {
								prison_brick_params.x = 0;
							}
							if (prison_brick_params.x > (display_x_size - prison_image_width)) {
								prison_brick_params.x = display_x_size - prison_image_width;
							}

							windowManager.updateViewLayout(prison_brick, prison_brick_params);
							windowManager.updateViewLayout(main_crab_ani, params);
							if (show_prison_hide_menu_enabled) {
								if (prison_brick_params.x > (display_x_size / 3)) {
									unlock_view_params.x = prison_brick_params.x - first_menu_poinster;
									canned_food_params.x = prison_brick_params.x - second_menu_poinster;
								} else {
									unlock_view_params.x = prison_brick_params.x + prison_image_width;
									canned_food_params.x = prison_brick_params.x + prison_image_width + first_menu_poinster;
								}
								unlock_view_params.y = prison_brick_params.y;
								canned_food_params.y = params.y;
								windowManager.updateViewLayout(unlock_view, unlock_view_params);
								windowManager.updateViewLayout(canned_food, canned_food_params);

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
			windowManager.removeView(lock_view);
			windowManager.removeView(canned_food);
			windowManager.removeView(lotto_ball);
			show_menu_enabled = false;
	}

	private void hide_food_menu() {
			windowManager.removeView(canned_food);
	}


	private void hide_shrimp_food() {
		windowManager.removeView(shrimp_food);
		show_shrimp_enabled = false;
	}

	private void hide_prision_menu2() {
		windowManager.removeView(unlock_view);
		show_prison_hide_menu_enabled = false;
	}

	private void hide_prison_menu() {
		windowManager.removeView(prison_brick);
		show_prison_enabled = false;
	}

	private void crab_hiding() {
		crab_smile_enable = false;
		s_current_image_num = 0;
		crab_hide_enable = true;
	}

	private void Set_Food_Position(int pos,int val) {
		switch(pos) {
			case POSITION_X:
				food_position[POSITION_X] = val;
				break;
			case POSITION_Y:
				food_position[POSITION_Y] = val;
				break;
			default:
				break;
		}
	}

	private int[] Get_Food_Position(){
		return food_position;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCountThread.stopThread();
		if (main_crab_ani != null) windowManager.removeView(main_crab_ani);
	}

	class Crab_Handler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int[] food;
			current_image_num++;
			if(current_image_num > 9) {
				current_image_num = 1;
			}
            if(show_shrimp_enabled) {
				food = Get_Food_Position();
				if(Current_Direction == UP || Current_Direction == DOWN) {
					if((food[POSITION_Y]/10) < (params.y / 10)) {
						Current_Direction = UP;
					} else if((food[POSITION_Y]/10) > (params.y / 10)) {
						Current_Direction = DOWN;
					} else {
						Current_Direction = LEFT;
					}
				} else {
					if((food[POSITION_X]/10) < (params.x / 10)) {
						Current_Direction = LEFT;
					} else if((food[POSITION_X]/10) > (params.x / 10)) {
						Current_Direction = RIGHT;
					} else {
						Current_Direction = UP;
					}
				}

				if(((params.x < (food[POSITION_X] + shrimp_food_size_x)) && (params.x > food[POSITION_X]) && ((params.y < (food[POSITION_Y] + shrimp_food_size_y)) && (params.y > food[POSITION_Y]))) ||
				(((params.x) < (food[POSITION_X] + shrimp_food_size_x)) && ((params.x) > food[POSITION_X]) && (((params.y + crab_size_height) < (food[POSITION_Y] + shrimp_food_size_y)) && ((params.y+crab_size_height) > food[POSITION_Y]))) ||
				(((params.x + crab_size_width) < (food[POSITION_X] + shrimp_food_size_x)) && ((params.x + crab_size_width) > food[POSITION_X]) && (((params.y) < (food[POSITION_Y] + shrimp_food_size_y)) && ((params.y) > food[POSITION_Y]))) ||
				(((params.x + (crab_size_width / 2)) < (food[POSITION_X] + shrimp_food_size_x)) && ((params.x + (crab_size_width / 2)) > food[POSITION_X]) && (((params.y + (crab_size_height / 2)) < (food[POSITION_Y] + shrimp_food_size_y)) && ((params.y+(crab_size_height/2)) > food[POSITION_Y]))) ||
				(((params.x + crab_size_width) < (food[POSITION_X] + shrimp_food_size_x)) && ((params.x + crab_size_width) > food[POSITION_X]) && (((params.y + crab_size_height) < (food[POSITION_Y] + shrimp_food_size_y)) && ((params.y+crab_size_height) > food[POSITION_Y]))))
				{
					hide_shrimp_food();
				}
			}



			if(fast_moving_enable) {
				fast_duration--;
				if(fast_duration == 0) {
					fast_moving_enable = false;
					Set_Thread_Speed(default_thread_speed);
				}
			}

			if(crab_smile_enable || (Get_Phone_status() == PHONE_RING)) {
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
					if(s_current_image_num > crab_hide_duration) {
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

	public class CountThread extends Thread implements Runnable {

		private boolean isPlay = false;

		public CountThread() {
			isPlay = true;
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

				switch(Get_Current_Direction()) {
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

				try { Thread.sleep(Get_Thread_Speed()); }
				catch (InterruptedException e) { e.printStackTrace(); }

			}

		}
	}

	public void DO_LOTTO_RANDOM() {
		int new_value[] = {0,0,0,0,0,0};
		int index;
		int loop;
		boolean choice_complete;
		boolean already_selected = false;
		int temp_value;

		index = 1;
		choice_complete = false;
        while(!choice_complete) {
			Random r = new Random();
			temp_value = r.nextInt(45) + 1;
			loop = 0;
			already_selected = false;
			while (loop < index) {
				if (new_value[loop] == temp_value) {
					already_selected = true;
				}
				loop++;
			}
			if(!already_selected) {
				new_value[index - 1] = temp_value;
				if(++index == 7) {
					choice_complete = true;
				}
			}
		}
		loop = 0;
		while(loop < 6) {
			Log.i(TAG, "Ball[" + Integer.toString(loop) + "] : " + Integer.toString(new_value[loop]) );
			loop++;
		}
		show_lotto_ball_enabled =  false;
	}

	public void get_smile_random() {
		int new_value;
		Random r = new Random();
		new_value = r.nextInt(1000);  // 0 ~ 4
		if((new_value <= 2) && (fast_duration == 0)) {
			crab_smile_enable = true;
		}
	}

	public int get_random_locate(int boundary, int size) {
		Random r = new Random();
		return r.nextInt(boundary - size);
	}

	public boolean get_fast_or_hide() {
		int new_value;
		Random r = new Random();
		new_value = r.nextInt(1000) + 1 ;
		return (new_value == (new_value/2)*2);
	}

	public int get_random_value(int current)
	{
		int new_value = current;
		int moving = 1;

		while(current == new_value) {
				Random r = new Random();
				new_value = r.nextInt(4);
//			Log.e(TAG, "Random Direction : " + Integer.toString(new_value));
			    Random moving_r = new Random();
				moving = moving_r.nextInt(10);
//			Log.e(TAG, "Random Movingspeed : " + Integer.toString(moving));
			if(fast_duration == 0) {
				if (moving > 6) {
					Set_Moving_Speed(8);
					Set_Thread_Speed(default_thread_speed / 2);
				} else {
					Set_Moving_Speed(3);
					Set_Thread_Speed(default_thread_speed);
				}
			}
			if(current == DOWN && new_value == UP) {
				new_value = current;
			}
		}
		return new_value;
	}

	public void Set_Moving_Speed(int val) {
		moving_speed = val;
	}
	public void run_moving_down() {

		try {
				paramsF.y = params.y + (moving_speed);

			if(show_prison_enabled) {
				if((prison_brick_params.y + (prison_image_height / 7 )) < paramsF.y) {
					mCountThread.Set_Current_Direction(get_random_value(DOWN));
					Set_Direction_Status(true);
				}
				else {
					windowManager.updateViewLayout(main_crab_ani, paramsF);
				}
			} else {
				if((display_y_size - (crab_size_height)) < paramsF.y) {
					mCountThread.Set_Current_Direction(get_random_value(DOWN));
					Set_Direction_Status(true);
//					if(SkipTimer_Direction_change != 0) {
//						windowManager.updateViewLayout(main_crab_ani, paramsF);
//					}
				}
				else {
					windowManager.updateViewLayout(main_crab_ani, paramsF);
				}
			}

		} catch (Exception ea) {
			Log.e(TAG, "Error : " + ea.toString());
		}
	}

	public void run_moving_left() {
//		fast_moving_enable = false;
		try {
				paramsF.x = params.x - (moving_speed);
			if(show_prison_enabled) {
				if((prison_brick_params.x + 20) >= paramsF.x) {
					mCountThread.Set_Current_Direction(get_random_value(LEFT));
					Set_Direction_Status(true);
				}
				else {
					windowManager.updateViewLayout(main_crab_ani, paramsF);
				}
			}else {
				if(0 >= paramsF.x) {
					mCountThread.Set_Current_Direction(get_random_value(LEFT));
					Set_Direction_Status(true);
//					if(SkipTimer_Direction_change != 0) {
//						windowManager.updateViewLayout(main_crab_ani, paramsF);
//					}
				}
				else {
					windowManager.updateViewLayout(main_crab_ani, paramsF);
				}
			}
		} catch (Exception ea) {
			Log.e(TAG, "Error : " + ea.toString());
		}
	}
	public void run_moving_right() {
 //       fast_moving_enable = false;
		try {
				paramsF.x = params.x + (moving_speed);

			if(show_prison_enabled) {
				if(((prison_brick_params.x + prison_image_width) - crab_size_width - 50) < paramsF.x) {
					mCountThread.Set_Current_Direction(get_random_value(RIGHT));
					Set_Direction_Status(true);
				}
				else {
					windowManager.updateViewLayout(main_crab_ani, paramsF);
				}
			} else {
				if((display_x_size - (crab_size_width)) < paramsF.x) {
					mCountThread.Set_Current_Direction(get_random_value(RIGHT));
					Set_Direction_Status(true);
//					if(SkipTimer_Direction_change != 0)
//					{
//						windowManager.updateViewLayout(main_crab_ani, paramsF);
//					}
				}
				else {
					windowManager.updateViewLayout(main_crab_ani, paramsF);
				}
			}


		} catch (Exception ea) {
			Log.e(TAG, "Error : " + ea.toString());
		}
	}
	public void run_moving_up() {
//        fast_moving_enable = false;
		try {
				paramsF.y = params.y - (moving_speed);

			if(show_prison_enabled) {
				if((prison_brick_params.y + 10) >= paramsF.y) {
					mCountThread.Set_Current_Direction(get_random_value(UP));
					Set_Direction_Status(true);
				}
				else {
					windowManager.updateViewLayout(main_crab_ani, paramsF);
				}
			}else {
				if(0 >= paramsF.y) {
					mCountThread.Set_Current_Direction(get_random_value(UP));
					Set_Direction_Status(true);
				}
				else {
					windowManager.updateViewLayout(main_crab_ani, paramsF);
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
						main_crab_ani.setImageResource(R.drawable.left_crab__x1);
						break;
					case TWO:
						main_crab_ani.setImageResource(R.drawable.left_crab__x2);
						break;
					case THREE:
						main_crab_ani.setImageResource(R.drawable.left_crab__x3);
						break;
					case FOUR:
						main_crab_ani.setImageResource(R.drawable.left_crab__x4);
						break;
					case FIVE:
						main_crab_ani.setImageResource(R.drawable.left_crab__x5);
						break;
					case SIX:
						main_crab_ani.setImageResource(R.drawable.left_crab__x6);
						break;
					case SEVEN:
						main_crab_ani.setImageResource(R.drawable.left_crab__x7);
						break;
					case EIGHT:
						main_crab_ani.setImageResource(R.drawable.left_crab__x8);
						break;
					case NINE:
						main_crab_ani.setImageResource(R.drawable.left_crab__x9);
						break;
					default:
						break;
				}
				break;
			case LEFT:
				switch (imgnum) {
					case ONE:
						main_crab_ani.setImageResource(R.drawable.right_crab__x1);
						break;
					case TWO:
						main_crab_ani.setImageResource(R.drawable.right_crab__x2);
						break;
					case THREE:
						main_crab_ani.setImageResource(R.drawable.right_crab__x3);
						break;
					case FOUR:
						main_crab_ani.setImageResource(R.drawable.right_crab__x4);
						break;
					case FIVE:
						main_crab_ani.setImageResource(R.drawable.right_crab__x5);
						break;
					case SIX:
						main_crab_ani.setImageResource(R.drawable.right_crab__x6);
						break;
					case SEVEN:
						main_crab_ani.setImageResource(R.drawable.right_crab__x7);
						break;
					case EIGHT:
						main_crab_ani.setImageResource(R.drawable.right_crab__x8);
						break;
					case NINE:
						main_crab_ani.setImageResource(R.drawable.right_crab__x9);
						break;
					default:
						break;
				}
				break;
			case LEFT_DOWN:
				switch(imgnum){
					case ONE:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x1);
						break;
					case TWO:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x2);
						break;
					case THREE:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x3);
						break;
					case FOUR:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x4);
						break;
					case FIVE:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x5);
						break;
					case SIX:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x6);
						break;
					case SEVEN:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x7);
						break;
					case EIGHT:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x8);
						break;
					case NINE:
						main_crab_ani.setImageResource(R.drawable.left_up_crab__x9);
						break;
					default:
						break;
				}
				break;
			case LEFT_UP:
				switch(imgnum){
					case ONE:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x1);
						break;
					case TWO:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x2);
						break;
					case THREE:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x3);
						break;
					case FOUR:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x4);
						break;
					case FIVE:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x5);
						break;
					case SIX:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x6);
						break;
					case SEVEN:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x7);
						break;
					case EIGHT:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x8);
						break;
					case NINE:
						main_crab_ani.setImageResource(R.drawable.left_down_crab__x9);
						break;
					default:
						break;
				}
				break;
			case RIGHT_DOWN:
				switch(imgnum){
					case ONE:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x1);
						break;
					case TWO:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x2);
						break;
					case THREE:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x3);
						break;
					case FOUR:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x4);
						break;
					case FIVE:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x5);
						break;
					case SIX:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x6);
						break;
					case SEVEN:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x7);
						break;
					case EIGHT:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x8);
						break;
					case NINE:
						main_crab_ani.setImageResource(R.drawable.right_up_crab__x9);
						break;
					default:
						break;
				}
				break;
			case RIGHT_UP:
				switch(imgnum){
					case ONE:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x1);
						break;
					case TWO:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x2);
						break;
					case THREE:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x3);
						break;
					case FOUR:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x4);
						break;
					case FIVE:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x5);
						break;
					case SIX:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x6);
						break;
					case SEVEN:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x7);
						break;
					case EIGHT:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x8);
						break;
					case NINE:
						main_crab_ani.setImageResource(R.drawable.right_down_crab__x9);
						break;
					default:
						break;
				}
				break;
			case HIDE_CRAB:
				switch(imgnum){
					case ONE:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x1);
						break;
					case TWO:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x2);
						break;
					case THREE:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x3);
						break;
					case FOUR:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x4);
						break;
					case FIVE:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x5);
						break;
					case SIX:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x6);
						break;
					case SEVEN:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x7);
						break;
					case EIGHT:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x8);
						break;
					case NINE:
						main_crab_ani.setImageResource(R.drawable.hide_crab__x9);
						break;
					default:
						break;
				}
				break;
			case SMILE_CRAB:
				if(show_prison_enabled) {
					switch (imgnum) {
						case ONE:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x1);
							break;
						case TWO:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x2);
							break;
						case THREE:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x3);
							break;
						case FOUR:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x4);
							break;
						case FIVE:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x5);
							break;
						case SIX:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x6);
							break;
						case SEVEN:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x7);
							break;
						case EIGHT:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x8);
							break;
						case NINE:
							main_crab_ani.setImageResource(R.drawable.dislike_crab__x9);
							break;
						default:
							break;
					}
				} else {
					if (Check_Audio_playing()) {
						switch (imgnum) {
							case ONE:
								main_crab_ani.setImageResource(R.drawable.music_crab__x1);
								break;
							case TWO:
								main_crab_ani.setImageResource(R.drawable.music_crab__x2);
								break;
							case THREE:
								main_crab_ani.setImageResource(R.drawable.music_crab__x3);
								break;
							case FOUR:
								main_crab_ani.setImageResource(R.drawable.music_crab__x4);
								break;
							case FIVE:
								main_crab_ani.setImageResource(R.drawable.music_crab__x5);
								break;
							case SIX:
								main_crab_ani.setImageResource(R.drawable.music_crab__x6);
								break;
							case SEVEN:
								main_crab_ani.setImageResource(R.drawable.music_crab__x7);
								break;
							case EIGHT:
								main_crab_ani.setImageResource(R.drawable.music_crab__x8);
								break;
							case NINE:
								main_crab_ani.setImageResource(R.drawable.music_crab__x9);
								break;
							default:
								break;
						}
					} else {
						switch (imgnum) {
							case ONE:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x1);
								break;
							case TWO:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x2);
								break;
							case THREE:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x3);
								break;
							case FOUR:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x4);
								break;
							case FIVE:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x5);
								break;
							case SIX:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x6);
								break;
							case SEVEN:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x7);
								break;
							case EIGHT:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x8);
								break;
							case NINE:
								main_crab_ani.setImageResource(R.drawable.smile_crab__x9);
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

	private boolean Get_Direction_Status(){
		return Direction_Changed;
	}

	private void Set_Direction_Status(boolean val) {
		Direction_Changed = val;
	}

	private boolean Check_Audio_playing() {
		AudioManager manager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
		return manager.isMusicActive();
	}

	private void Set_Thread_Speed(int val) {
		thread_speed = val;
	}
	private int Get_Thread_Speed(){
		return thread_speed;
	}
}


