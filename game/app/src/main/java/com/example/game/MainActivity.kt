package com.example.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // Declare views and game parameters
    private lateinit var scoreTextView: TextView
    private lateinit var paddleView: View
    private lateinit var ballView: View
    private lateinit var brickContainer: LinearLayout

    private var ballXPosition = 0f
    private var ballYPosition = 0f
    private var ballXSpeed = 0f
    private var ballYSpeed = 0f
    private var paddleXPosition = 0f
    private var score = 0
    private var brickRows = 9
    private var brickColumns = 10
    private var brickWidth = 100
    private var brickHeight = 40
    private var brickMargin = 4
    private var lives = 3

    // Initialize game layout and start new game
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        scoreTextView = findViewById(R.id.scoreText)
        paddleView = findViewById(R.id.paddle)
        ballView = findViewById(R.id.ball)
        brickContainer = findViewById(R.id.brickContainer)

        // Start new game when new game button is clicked
        val newGameButton = findViewById<Button>(R.id.newgame)
        newGameButton.setOnClickListener {
            setupBricks()
            beginGame()
            newGameButton.visibility = View.INVISIBLE
        }
    }

    // Set up brick layout
    private fun setupBricks() {
        val brickWidthWithMargin = brickWidth + brickMargin

        for (row in 0 until brickRows) {
            val rowLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = params

            for (col in 0 until brickColumns) {
                val brick = View(this)
                val brickParams = LinearLayout.LayoutParams(brickWidth, brickHeight)
                brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin)
                brick.layoutParams = brickParams
                brick.setBackgroundResource(R.drawable.ic_launcher_background)
                rowLayout.addView(brick)
            }

            brickContainer.addView(rowLayout)
        }
    }

    // Move the ball on the screen
    private fun moveBall() {
        ballXPosition += ballXSpeed
        ballYPosition += ballYSpeed
        ballView.x = ballXPosition
        ballView.y = ballYPosition
    }

    // Move the paddle according to touch input
    private fun movePaddleOnTouch(x: Float) {
        paddleXPosition = x - paddleView.width / 2
        paddleView.x = paddleXPosition
    }

    // Check for collisions between ball and other objects
    @SuppressLint("ClickableViewAccessibility")
    private fun checkCollisions() {
        // Check collision with walls
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        if (ballXPosition <= 0 || ballXPosition + ballView.width >= screenWidth) {
            ballXSpeed *= -1
        }

        if (ballYPosition <= 0) {
            ballYSpeed *= -1
        }

        // Check collision with paddle
        if (ballYPosition + ballView.height >= paddleView.y && ballYPosition + ballView.height <= paddleView.y + paddleView.height
            && ballXPosition + ballView.width >= paddleView.x && ballXPosition <= paddleView.x + paddleView.width
        ) {
            ballYSpeed *= -1
            score++
            scoreTextView.text = "Score: $score"
        }

        // Check collision with bottom wall (paddle misses the ball)
        if (ballYPosition + ballView.height >= screenHeight - 100) {
            lives--
            if (lives > 0) {
                Toast.makeText(this, "$lives balls left", Toast.LENGTH_SHORT).show()
            }

            if (lives <= 0) {
                endGame()
            } else {
                resetBallAndPaddle()
                beginGame()
            }
        }

        // Check collision with bricks
        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout
            val rowTop = rowLayout.y + brickContainer.y

            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View

                if (brick.visibility == View.VISIBLE) {
                    val brickLeft = brick.x + rowLayout.x
                    val brickRight = brickLeft + brick.width
                    val brickTop = brick.y + rowTop
                    val brickBottom = brickTop + brick.height

                    if (ballXPosition + ballView.width >= brickLeft && ballXPosition <= brickRight
                        && ballYPosition + ballView.height >= brickTop && ballYPosition <= brickBottom
                    ) {
                        brick.visibility = View.INVISIBLE
                        ballYSpeed *= -1
                        score++
                        scoreTextView.text = "Score: $score"
                        return
                    }
                }
            }
        }
    }

    // Reset ball position and paddle position after game over
    private fun resetBallAndPaddle() {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        ballXPosition = screenWidth / 2 - ballView.width / 2
        ballYPosition = screenHeight / 2 - ballView.height / 2 + 525

        ballView.x = ballXPosition
        ballView.y = ballYPosition

        ballXSpeed = 0f
        ballYSpeed = 0f

        paddleXPosition = screenWidth / 2 - paddleView.width / 2
        paddleView.x = paddleXPosition
    }

    // Handle game over scenario
    private fun endGame() {
        scoreTextView.text = "Game Over"
        score = 0
        val newGameButton = findViewById<Button>(R.id.newgame)
        newGameButton.visibility = View.VISIBLE
    }

    // Move paddle on touch input
    @SuppressLint("ClickableViewAccessibility")
    private fun movePaddle() {
        paddleView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    movePaddleOnTouch(event.rawX)
                }
            }
            true
        }
    }

    // Start the game
    private fun beginGame() {
        movePaddle()
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()
        paddleXPosition = screenWidth / 2 - paddleView.width / 2
        paddleView.x = paddleXPosition
        ballXPosition = screenWidth / 2 - ballView.width / 2
        ballYPosition = screenHeight / 2 - ballView.height / 2

        ballXSpeed = 3 * screenDensity
        ballYSpeed = -3 * screenDensity

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = Long.MAX_VALUE
        animator.addUpdateListener { animation ->
            moveBall()
            checkCollisions()
        }
        animator.start()
    }
}
