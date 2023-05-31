@file:OptIn(ExperimentalMaterial3Api::class)
package com.h2square.mycalculater

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column



import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.h2square.mycalculater.MainActivity.Companion.TAG
import com.h2square.mycalculater.ui.theme.ActionBtnColor
import com.h2square.mycalculater.ui.theme.MyCalculaterTheme
import com.h2square.mycalculater.ui.theme.Purple40
import com.h2square.mycalculater.ui.theme.PurpleGrey80
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty


class MainActivity : ComponentActivity() {

    companion object {
        const val TAG ="메인"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCalculaterTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    Calculater()


                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calculater(){

    val numbers = listOf<Int>(0,1,2,3,4,5,6,7,8,9)
val actions: Array<CalculateAction> = CalculateAction.values()

    val buttons = listOf(
        CalculateAction.Divide,
        7,8,9,CalculateAction.Multiply,
        4,5,6,CalculateAction.Minus,
        1,2,3,CalculateAction.Plus,
        0
    )

    var firstInput by remember{ mutableStateOf("0") }

    var secondInput by remember{ mutableStateOf("") }

    val selectedAction : MutableState<CalculateAction?> = remember {
        mutableStateOf(null)
    }

    val selectedSymbol : String = selectedAction.value?.symbol?:""

    val calculatehistories :MutableState<List<String>> = remember{ mutableStateOf(emptyList()) }


        val corutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
        var isHistoryvisible by remember {
            mutableStateOf(true)
        }


    Column(modifier = Modifier.fillMaxSize()) {
        
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            onClick = {
                isHistoryvisible =! isHistoryvisible

            }
        ) {
            Text(
            text = if (isHistoryvisible==true)"계산기록 안보기" else "계산기록 보기 ",
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .padding(3.dp)
            )
        }
        AnimatedVisibility(visible = isHistoryvisible, modifier = Modifier.weight(1f)) {
            LazyColumn(state=scrollState,

                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                reverseLayout = true,
                content = {
                    items(calculatehistories.value){aHistory ->
                        Text(text = aHistory, modifier = Modifier.background(PurpleGrey80))
                    }
                })
        }


        Spacer(modifier = Modifier.weight(if (isHistoryvisible)0.1f else 1f))
        
        LazyVerticalGrid(columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                item(span = { GridItemSpan(maxLineSpan) }){
                    NumberText(firstInput , secondInput , selectedSymbol , modifier = Modifier.fillMaxSize())

                }

                item(span = { GridItemSpan(2) }){
                    ActionBtn(action = CalculateAction.AllClear,
                        onClicked = {
                            firstInput="0"
                            secondInput=""
                            selectedAction.value=null
                        })
                }
                item(span = { GridItemSpan(1) }){
                    ActionBtn(action = CalculateAction.Del, onClicked = {
                        //선택된 연산푱를 지울떄
                        //두번째입력을 지울때
                        if (secondInput.length>0){
                            secondInput = secondInput.dropLast(1)
                            return@ActionBtn
                        }

                        if (selectedAction.value!=null){
                            selectedAction.value=null
                            return@ActionBtn
                        }//연산도 없고 두번째 입력도 없을때
                        firstInput= if (firstInput.length==1)"0" else firstInput.dropLast(1)
                    })
                }


                items(buttons){ aButton->
                    when(aButton){
                        is CalculateAction -> ActionBtn(action = aButton,selectedAction.value, onClicked = {
                            selectedAction.value=aButton


                        })
                        is Int ->NumberBtn(number = aButton, onClicked = {

                            if (selectedAction.value ==null){
                                if (firstInput=="0") firstInput = aButton.toString() else firstInput+=aButton

                            }else{
                                secondInput+=aButton
                            }
                            //연산 액션 선택시 두번째 입력

                        })

                    }

                }

                item(span = { GridItemSpan(maxCurrentLineSpan) }){
                    ActionBtn(action = CalculateAction.Calcultae, onClicked = {
                        if (secondInput.isEmpty()){
                            return@ActionBtn
                        }

                        selectedAction.value?.let {

                            val result= doCalculate(firstNumber = firstInput.toFloat(),
                                secondNumber = secondInput.toFloat(),
                                action = it
                            )
                            //ㄱㅖ산기록업데이트
                            val caculationhistory = "$firstInput $selectedSymbol $secondInput = $result"
                            calculatehistories.value += caculationhistory

                            corutineScope.launch { scrollState.animateScrollToItem(calculatehistories.value.size) }
                            firstInput =result.toString()
                            secondInput= ""
                            selectedAction.value=null
                            Log.d(TAG,"계산 결과 :$result")
                        }?: Log.d(TAG,"선택된 연산이없습니다")
                    })
                }



            }
        )
    }

}

@Composable
fun NumberText(firstInput:String,
               secondInput:String,
               selectedSymbol: String,
               modifier: Modifier
){
    Row(modifier = Modifier, verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp,
        Alignment.End
    )) {
        Text(text = firstInput,
            fontSize = 50.sp,
//            modifier=Modifier.background(Color.Yellow),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 50.sp,
            maxLines = 1,
            color = Color.Black
        )
        Text(text =selectedSymbol,
            fontSize = 50.sp,
//            modifier=Modifier.background(Color.Yellow),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 50.sp,
            maxLines = 1,
            color = Purple40
        )
        Text(text = secondInput,
            fontSize = 50.sp,
//            modifier=Modifier.background(Color.Yellow),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 50.sp,
            maxLines = 1,
            color = Color.Black
        )
    }
}
fun doCalculate(
    firstNumber: Float,
    secondNumber:Float,
    action: CalculateAction

):Float?{
    return when(action){
        CalculateAction.Plus ->firstNumber+secondNumber
        CalculateAction.Minus -> firstNumber-secondNumber
        CalculateAction.Multiply-> firstNumber*secondNumber
        CalculateAction.Divide -> firstNumber/secondNumber
        else-> null

    }

}


@Composable
fun NumberBtn(number:Int,onClicked :()->Unit){
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        onClick = onClicked
    ) {

        Text(text = number.toString(), Modifier
            .padding(16.dp)
            .fillMaxSize()
            , textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionBtn(action: CalculateAction,selectedAction:CalculateAction? =null , onClicked: (() -> Unit)?=null){

    val isSelected : Boolean = selectedAction ==action
    val cardContainer :Color = if (isSelected) Purple40 else ActionBtnColor

    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedAction==action) Purple40 else ActionBtnColor ,
        contentColor =if (selectedAction==action) Color.White else Color.Black
        ),
        onClick = {
            onClicked?.invoke()
            Log.d(TAG,"CARD가 클릭되었다")
        }
    ) {

        Text(text = action.symbol, Modifier
            .padding(16.dp)
            .fillMaxSize()
            , textAlign = TextAlign.Center,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold)
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyCalculaterTheme {
        Greeting("Android")
    }
}