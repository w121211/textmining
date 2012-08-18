package com.noodle.textmining;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import taobe.tec.jcc.JChineseConvertor;

import com.aliasi.spell.TfIdfDistance;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class TfIdfDistanceDemo {

	private static Properties prop;

	public static void main(String[] args) throws IOException {
		// load a properties file
		prop = new Properties();
		prop.load(new FileInputStream("config.properties"));

		// connect to database
		ODatabaseDocumentTx db;
		db = new ODatabaseDocumentTx(prop.getProperty("DB_DIR")).open("admin", "admin");

		// String[] docs = {
		// "機車#NN 經銷商#NN 準備#NN 進行#NN 保養#VV nikita#NN 300#CD 經銷商#NN 備料#NN 沒了#NN 所以#AD 須#VV 從#NN 公司#NN 叫#VV",
		// "機車#NN 經銷商#NN 準備#NN 進行#NN 保養#VV nikita#NN 300#CD 經銷商#NN 備料#NN 沒了#NN 所以#AD 須#VV 從#NN 公司#NN 叫#VV 料#NN 很#AD 正常#VA 的#DEC 手續#NN 沒影#NN 響#NN 就#AD 等#VV 吧#SP 但是#AD 經銷商#NN 打電話#NR 給#NN 公司#NN 進行#VV 零件#NN 叫#VV 料#NN 後#LC 得到#VV 的#DEC 消息#NN 令人#NN 錯#VV 愕#NN 從#VV 101#CD 08#M 13#CD 開始#M 光陽#NN 底下#NN 正#AD 廠#VV 零件#NN 商#NN 停止#VV 供應#NN 光大#NN 重#AD 車#VV 零件#NN 給#AD 未#AD 簽#VV 重#AD 車#VV 銷售#NN 點#VV 約#NN 的#DEC 普通#JJ 經銷商#NN 也就是說#AD 國產#VV 光陽#NN 重#AD 車#VV 的#DEC 優勢#NN 之一#NN 零件#NN 好#AD 叫#VV 徹底#NR 消失#VV 影響#VV 到#VV 的#DEC 車種#NN 只要#AD 是#VC 屬於#NN 光大#NN 重#AD 車#VV 的#DEC 產品#NN 均無#NN 法#NN 透過#VV 普通#JJ 經銷商#NN 叫#VV 料#NN nikita#NN 200#CD 300#M xciting#NN 250#CD 300#M 500#CD myroad#M 700#CD shadow#M 300#CD 這#M 幾#VV 台#NR 全部#AD 必須#VV 透過#VV 光陽#NN 官方#NN 的#DEG 重#JJ 車#NN 經銷商#NN 進行#VV 維修#NN 又#AD 或者#CC 普通#JJ 經銷商#NN 須#VV 透過#VV 重#AD 車#VV 經銷商#NN 來#NN 叫#VV 零件#NN 這#NN 勢必#AD 造成#VV 許多#JJ 車主#NN 的#DEG 麻煩#NN 起因#NN 必須#VV 詢問#NN 光陽#NN 官方#NN 才有#NN 解答#VV 但是#AD 我想#VV 光陽#NN 不#AD 會理#VV 一個#NN end#NN user#NN 的#DEG 問題#NN 光陽#NN 在#P 早期#NN 時段#NN 已經#NN 下令#VV 禁止#VV 正#AD 廠#VV 材料#NN 中心#NN 販#VV 售#VV 零件#NN 給#NN 車主#NN 現在#VV 又#AD 進一步#AD 下令#VV 重#AD 車#VV 耗#VV 材#NN 零件#NN 等#ETC 東西#NN 禁止#VV 供應#NN 給#AD 非#VC 簽約#NN 經銷商#NN 光陽#NN 阿#NR 光陽#VV 你#PN 到底#AD 在想#VV 什麼#NN 為什麼#NN 為了#AD 少數#VV 店家#NN 的#DEG 利益#NN 犧牲#NN 大多數#NN 使用者#NN 的#DEG",
		// "福利#NN razgriz#NN and#CC mobius#NN 1#CD shoot#NN dowm#NN that#NN devil#NN as#NN fast#NN and#CC clean#NN as#NN you#NN guys#AD can#VV rick#NN wrote#NN 昨天#NT 小弟#NN 一如#AD 往常#NN 的#DEG 恕#NN 刪#NN 本來#NN 就是#AD 有等級#VV 之分#NN 因為#NN 光陽#NN 要#VV 塑造#VV 重#AD 車#VV 經銷商#NN 以做#VV 區隔#NN 才能#NN 有#VE 穩定#VV 的#DEC 利潤#NN 就#AD 好比#VV lexus#NN 的#DEC 車#NN 跟#CC toyota#NN 是#VC 同#P 老板#NN 同#P 集團#NN 但#AD 你#PN 開#VV lexus#NN 總不#NN 能#VV 跑去#VV toyota#NN 說#NN 要#VV 調#VV 零件#NN 吧#SP 使用者#NN 的#DEG 福利#NN 嗯#IJ 使用者#NN 的#DEG 福利#NN 就是#AD 來#VV 造成#VV 公司#NN 經銷商#NN 的#DEG 困擾#NN 跟#CC 管理#NN 問題#NN 你是#NN 公司#NN 老板#NN 你#PN 怎麼#VV 做#VV 鎖#NN 供#NN 料#NN 給#VV 專屬#NN 的#DEC 經銷商#NN 是#VC 很#AD 正常#VA 的#DEC 體制#NN 國外#NN 車#NN 店#NN 也是#AD 這樣#VV 不然#AD 誰#VV 願意#NN 拿錢#VV 加入#VV 重#AD 車#VV 經銷商#NN 又不#AD 是#VC 做#VV 慈善事業#NN kyoufu294#NN wrote#NN 本來#NN 就是#AD 有等級#VV 之分#NN 因為#NN 光陽#NN 要#VV 塑造#VV 重#AD 車#VV 經銷商#NN 以做#VV 區隔#NN 才能#NN 有#VE 穩定#VV 的#DEC 利潤#NN 就#AD 好比#VV lexus#NN 的#DEC 車#NN 跟#CC toyota#NN 是#VC 同#P 老板#NN 同#P 集團#NN 但#AD 你#PN 開#VV lexus#NN 總不#NN 能#VV 跑去#VV toyota#NN 說#NN 要#VV 調#VV 零件#NN 吧#SP 抱歉#VV 小弟#NN 覺得#VV 您#PN 舉#VV 的#DEC 這個#NN 例子#NN 不太#AD 好#AD 因為#VV 再#AD 怎樣#VV 這#NN 兩家#NN 都是#AD 不同#VA 標誌#NN 而#MSP 光陽#VV 機車#NN vs#NN 光陽#NN 大型#JJ 重型#JJ 機車#NN 掛#VV 的#DEC 都是#NN 光陽#NN 的#DEG 標誌#NN 在車上#AD 哩#VV kyoufu294#VV wrote#NN 鎖#NN 供#VV 料#NN 給#VV 專屬#NN 的#DEC",
		// "經銷商#NN 是#VC 很#AD 正常#VA 的#DEC 體制#NN 國外#NN 車#NN 店#NN 也是#AD 這樣#VV 不然#AD 誰#VV 願意#NN 拿錢#VV 加入#VV 重#AD 車#VV 經銷商#NN 經銷商#NN 是#VC 賣#VV 車#NN 的#DEC 為主#NN 吧#SP 維修#VV 為何#NN 也要#AD 限#VV 料#NN 供應#NN 換換#NN 機油#NN 機油#NN 濾#VV 芯#NN 更換#VV 空#NN 濾#NN 芯#VV 更換#VV 這種#NN 再#AD 普通#VA 不過#VA 的#DEC 事情#NN 非得#AD 要#VV 重#AD 車#VV 經銷商#NN razgriz#NN and#CC mobius#NN 1#CD shoot#NN dowm#NN that#NN devil#NN as#NN fast#NN and#CC clean#NN as#NN you#NN guys#AD can#VV rick#NN wrote#NN 經銷商#NN 是#VC 賣#VV 車#NN 的#DEC 為#NN 恕#NN 刪#VV 我#PN 覺得#VV 光陽#NN 重點#NN 不#AD 是在#VV 消費者#NN 而是#AD 他#PN 要#VV 逼#VV 一般#JJ 經銷商#NN 加入#VV 等級#JJ 更高#JJ 的#DEG 重#NN 車#NN 經銷#NN 可能#VV 光陽#AD 想把#VV 重#AD 車#VV 塑造#VV 成#VV 精品#NN 也#AD 必需#VV 透過#VV 像#P ken#NR 一樣#AD 等級#VV 的#DEC 經銷商#NN 來#NN 服務#VV 這樣才能#VV 鼓#VV 厲#VV 一般#JJ 經銷商#NN 砸#VV 錢#VV 去#VV 提升#VV 自己的#JJ 店#NN 不然#AD 一般#AD 路邊#VV 破破#VV 的#DEC 通路#NN 店#NN 就可以#AD 賣#VV 重#AD 車#VV 那個#NN 格#VV 不太#AD 一樣#VV 我#PN 個人#NN 猜測#NN 是#VC 這樣#NN 當然#AD 老板#NN 想#VV 啥#VV 我們#NN 是#VC 不容易#AD 知道#VV 得#DER rick#VV wrote#NN 昨天#NT 小弟#NN 一如#AD 往常#NN 的#DEG 恕#NN 刪#NN 唉#SP 金#JJ 彄#NN 憐#NN 這家#NN 車#VV 廠#NN 已經#NN 對#VV 他很#NN 失望#VV 了#AS 自己#PN 換#VV 機油#NN 跟#CC 空#NN 濾#NN 一向#AD 不#AD 手軟#VV 結果#NN 車子#NN 還是#AD 吃#VV 機油#VV 了#AS 而且還#NN 吃#VV 不小#AD 每#DT 500#CD 公里#M 要#VV 加#VV 200cc#CD 左右#LC 的#DEG 機油#NN 兩萬#NN 之前#LC 還很#VV 驕傲#NN 我的#NN 車#VV 雖然#AD 彎道#VV",
		// "很#AD 弱#VA 平衡#AD 做#VV 不好#AD 至少#AD 我#PN 拿到#VV 不會#AD 吃#VV 機油#NN 的#DEC 一台#JJ 加速#NN 也#AD 很#AD 優#VV 省#NN 油#NN 度#VV 也還#VV 不賴#JJ 沒想到#NN 過了#VV 兩萬#NN 車子#NN 平衡#NN 做#VV 的#DEC 不好#AD 要#VV 重#AD 灌#VV 前#JJ 叉#NN 油#NN 傳動#NN 會#VV 打滑#VV 要#VV 換#VV 改裝#VV 大碗#NN 公#NN 離合器#NN 之後#NN 因為#NN 換#VV 100#CD 90w#NN 的#DEG 大#JJ 燈#NN 融#VV 反射#NN 面#NN 這是#AD 燈泡#VV 的#DEC 錯#NN 還有#VV 賣#VV 燈泡#VV 的#DEC 賣#NN 家#NN 說不#NN 會#VV 融#VV 的#DEC 錯#NN 買了#NN 一顆#AD 新的#VV 大#JJ 燈#NN 跟#CC 用#P 之前#LC 不會#VV 融#VV 反射#NN 面的#NN 85#CD 80w#NN 之後#NN 居然#AD 融#VV 反射#NN 面#NN 現在#VV 原本#AD 是#VC 國產#VV 重#AD 車#VV 的#DEC 優勢#NN 居然#AD 也#AD 自己#PN 搞#VV 掉#VV 整個#VV 無言#NN 朋友#NN 有說#VV 要買#NN 他們#NN 家#NN 的#DEG 重#JJ 車#NN 還好#VV 我#PN 跟他#VV 說#VV 要#VV 緩#VV 一下#AD 看一下#VV 後續#VV 的#DEC 狀況#NN 再說#NN 沒想到#VV 還#NN 真的#AD 有#VE 狀況#NN 現在#VV 要多#NN 囤#NN 一點#AD 耗#VV 材#NN 不然#AD 就是#AD 存錢#VV 準備#NN 換車#NN 車#VV 彪#NN 虎#NN 等我#VV ps#NN 某#DT 幹#NN 版#NN 有人#PN 報#VV 料#NN 南部#NN 某#DT 縣市#NN 材料#NN 行#NN 有#VE 重#JJ 車#NN 料#NN kyoufu294#VV wrote#NN 本來#NN 就是#AD 有等級#VV 之分#NN 不然#AD 誰#VV 願意#NN 拿錢#VV 加入#VV 重#AD 車#VV 經銷商#NN 又不#AD 是#VC 做#VV 慈善事業#VV 我#PN 終於#NN 了解#VV 2012#CD 8#CD 13#CD 日#M 之前#LC 的#DEG ken#NR 店#NN 都是#AD 在做#VV 慈善事業#NR 維修#VV 體系#NN 技術#NN 爛#VV 鎖#NN 不住#AD 維修#VV 市場#NN 只好#AD 從#VV 零件#NN 上#LC 撈#VV 唉#SP 我的#AD nikita300#VV 才#AD 剛#VV 買#VV 一#CD 個月#M 還#NN 被#SB 綁#VV 約#NN 兩年#NT 不能#AD 轉手#VV 看到#VV 這樣#NN 悲劇#VV 了#AS 而且#AD 一般#JJ 店家#NN 也#AD 不會#AD 砸#VV 個#VV 幾百#CD 萬#M 去#VV 升級#VV ken#NR 店#NN 吧#SP 那#DT 如果#CS 我#PN 在#P 一般#JJ 車行#NN 買的#NN 如果#CS 真的#AD 叫#VV 不到#AD 料#VV 那#DT 我#PN 能去#VV 修車#VV 的#DEC 點#NN 真的#AD 很少#AD 耶#VV 若是#CS 這樣#VV 兩年#NT 過了#NN 貸款#NN 繳#VV 完#VV 我還#NN 不如#VV 把#BA 車#NN 賣掉#VV 去#VV 買進#VV 口#NN 重#AD 車#VV 反正#AD 叫#VV 料#NN 都不#NN 容易#AD 維修#VV 一樣#AD 要等#JJ 差別#NN 只是#AD 進口#VV 叫#VV 料#NN 貴#NN 但#AD 論#VV 品質#NN 進口#NN 還是#AD 比較#VV 耐#VV 操#VV 一點#AD 光#AD 養#VV 這樣#NN 不是#CC 砸了#VV 自己#PN 重#AD 車#VV 的#DEC 優勢#NN 嗎#VV 這樣#NN 叫#VV 我們#NN 買#VV 他#PN 重#AD 車#VV 的#DEC 車主#NN 情#NN 何以#AD 堪#VV kohonen#NR wrote#NN 我#PN 終於#AD 了解#VV 2012#CD 恕#M 刪#NN 盤子#NN 在#P 我#PN 就在#VV 銘#AD 言#VV 阿#NR 僅#NN 開放#VV 少數#VV ken#NR 重機#NN 經銷#NN 據點#NN 與#VV 維修#VV 服務站#NN 能夠#NN 向#P 總公司#NN 訂#VV 料#NN 此舉#NN 將#NN 嚴重影響#VV 光陽#NN 重機#NN 零件#NN 通路#NN 與#AD 待#VV 料#NN 優勢#NN 對於#VV 既有#JJ 的#DEG 維修#NN 體系#NN 生態#NN 更是#AD 一大#AD 考驗#VV 單憑#NN 幾間#NN ken#NR 與#VV 維修#VV 站#VV 連#NN 保#NN 固#AD 期內#VV 車種#VV 出#VV 保#NR 固#AD 電#VV 系#NN 油氣#NN 滲#NN 油#NN 珠#NR 碗#M 頓#NN 點#VV 問題#NN 都要#NN 留#VV 車#NN 處理#VV 了#AS 再加#NN 上#LC 已經#VV 過#NN 保#VV 的#DEC 車種#NN 要#VV 排#VV 程#NN 維修#VV 只會#NN 延宕#VV 整體#NN 維修#VV 日程#NN 已經#NN 跟#CC 總公司#NN 簽約#VV 的#DEC ken#NR 店家#NN 表示#VV 要#VV 發達#VV 了#AS ken#NR 學徒#NN 維修#VV 服務站#NN 技師#NN 表示#VV 老子#NN 領#VV 的#DEC 薪水#NN 是#VC 死的#NN 幫#VV 老闆#NN 或#CC 公司#NN 多#AD 修#VV 幾#NN 台車#NN 又沒#NN 獎金#NN 時間#NN 到了#P 下班#NN 卡#VV 要緊#NN 管#VV 你#PN 車子#NN 修#VV 不修#VV 得#DER 完#VV 奧#NN 客#NN 別#NN 催#VV 留#VV 車#NN 慢慢#AD 排#VV 就#AD 對了#VV 曾經#NR 挺#VV 光陽#NN 重機#VV 的#DEC 車#NN 友#NN 表示#VV 哪天#NN 車子#NN 在#P 路上#NN 拋錨#NN 被#LB 一般#JJ 車行#NN 拒#VV 修#VV 怎麼辦#NN 反而#AD 讓#VV 有能力#JJ 維修#NN 重機#VV 的#DEC 車行#NN 與#NN 個人#NN 正#AD 廠#VV 零件#NN 取得#VV 不易#JJ 國產#NN 重機#VV 的#DEC 零件#NN 優勢#NN 不再#AD 這樣#VV 搞#VV 光陽#NN 誰#VV 挺#VV 你#PN 倒不如#VV 多花#AD 點錢#VV 去#VV 買#VV 妥善#AD 率#VV 高的#NN 進口#NN 重機#NN 還#VV 比較#NN 爽#VV 唉#SP 台#NR 灣#VV 廠商#NN 總是#NN 唉#SP 就#AD 好像#VV 你#PN 今天#NT 去#VV 牽#VV 了#AS 一頭#AD 驢#VV 每天#NN 餵#VV 他#PN 草#NN 幫#VV 牠#NN 梳毛#NN 吃草#VV 的#DEC 驢#NN 卻#NN 永遠#AD 只是#AD 驢#VV 不會#AD 是#VC 吃草#VV 的#DEC 牛#NN or#NN 馬#NN 就算#AD 從#VV 台北#NR 牽#VV 到#VV 高雄#NR 也#AD 一樣#VV"
		// };

		List<String> docs = new ArrayList<String>();
		for (ODocument doc : db.browseClass("Thread")) {
			docs.add((String) doc.field("pos_text_simp"));
		}

		// TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		// TokenizerFactory tokenizerFactory = new RegExTokenizerFactory("(\\S)+(#NN|#VV)");
		
		// Calculate TF-IDF
		TokenizerFactory tokenizerFactory = new RegExTokenizerFactory(
				"(\\S)+(#NN)");
		TfIdfDistance tfIdf = new TfIdfDistance(tokenizerFactory);
		for (String s : docs)
			tfIdf.handle(s);
		List<TfidfWord> keywords = new ArrayList<TfidfWord>();
		for (String term : tfIdf.termSet()) {
			// System.out.print(term + " ");
			keywords.add(new TfidfWord(
					term, tfIdf.docFrequency(term) * tfIdf.idf(term)));
		}

		// Write keywords to the file
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
				"./log/keywords.txt")));
		writer.println("Term, Tf-idf");
		Collections.sort(keywords, new ValueComparator());
		Collections.reverse(keywords);
		for (TfidfWord w : keywords) {
			writer.printf("%s:%.2f\n", w.word, w.tfidf);
		}
		writer.close();

		System.out.println("Process completed");
	}
}

class TfidfWord {
	public String word;
	public Double tfidf;

	public TfidfWord(String word, Double tfidf) throws IOException {
		JChineseConvertor chineseConverter = JChineseConvertor.getInstance();
		this.word = chineseConverter.s2t(word);
		this.word = this.word.replaceAll("#NN|#VV", "");
		this.tfidf = tfidf;
	}
}

class ValueComparator implements Comparator<TfidfWord> {
	public int compare(TfidfWord a, TfidfWord b) {
		return a.tfidf.compareTo(b.tfidf);
	}
}
