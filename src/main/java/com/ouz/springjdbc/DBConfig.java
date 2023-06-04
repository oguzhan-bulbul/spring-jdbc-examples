package com.ouz.springjdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class DBConfig {

  /** */
  public static String url = "jdbc:postgresql://localhost:5432/postgres";

  public static String username = "postgres";

  public static String password = "12345";

  /**
   * Hikari Connection Pool Ozellikleri
   *
   * <p>autoCommit - Pool'dan gelen connectionlara ait auto commit degerini tutar. Boolean bir
   * degiskendir. Default degeri true'dur.
   *
   * <p>connectionTimeOut - Pool'dan connection alabilmek icin beklenecek maximum sureyi ifade eder.
   * Eger bu sure asilirsa SQLException firlatilir. En dusuk 250ms olabilir. Default degeri 30000ms.
   *
   * <p>idleTimeout - Bu parametre bir connection'in poolda idle durumunda ne kadar sure boyunca
   * kalacagini belirler. Bu ozellik sadece minimumIdle degerinin maximumPoolSize'dan az oldugu
   * zaman gecerlidir. Bir connection maximum 30 saniye,ortalama olarak 15 saniye icerisinde idle
   * konumuna gecerler. 0 degeri verilirse idle connectionlar pool'dan asla silinmezler. Minimum
   * verilebilecek deger 10000ms'dir.Default deger ise 600000ms (10dk)'dir.
   *
   * <p>keepAliveTime - Bu ozellik bir connection'in ne kadar sure ile acik kalacagini belirler. Db
   * veya network altyapisindan timeout yemesini beklememek amaciyla kullanilir. Minimum degeri
   * 30000ms (30s) olarak belirlenmistir. Default degeri 0'dir. Bu DB'den timeout olana kadar
   * baglantinin canli kalacagini ifade eder.
   *
   * <p>maxLifetime - Bu ozellik bir connection'nin pooldaki maximum yasam zamanini ifade eder.
   * Kullanimdaki bir connection asla silinmez. Ancak kapatildigi zaman silinebilir. Bu parametrenin
   * ayarlanmasi kesinlikle onerilir. Ve onerilen deger ise Database veya Network Altyapisinin
   * maximum baglanti suresinden biraz kisa olmasidir. 0 degeri verilirse sonsuz maximumLifetime'i
   * ifade eder. Minimum verilebilecek 30000ms(30sn)dir. Defaullt olarak 1800000ms(30dk)'dir.
   *
   * <p>minimumIdle - Bu ozellik Hikari'nin pool'da idle pozisyonunda minimum kac adet connection
   * tutacagini ifade eder.Eger idle durumdaki connectionlar bu degerin altindaysa ve pooldaki
   * toplam connection sayisi maximumPoolSize degerinden daha kucukse, Hikari hizlica pool'a yeni
   * connectionlar eklemeye calisacaktir. Fakat maximum performans ve degisken isteklere karsi hizli
   * yanit verebilmek adina bu parametrenin ayarlanmasi pek onerilmez.Hikarinin fixed size bir pool
   * olarak davranmasini saglamak daha cok onerilir. Default olarak bu deger maximumPoolSize
   * degerine esitlenir.
   *
   * <p>maximumPoolSize - Bu ozellik idle olanlar ve kullanimda olanlar dahil pool'da maximum kac
   * adet connection olacagini sinirlar.Temel olarak bu deger backend ile db arasinda ayni anda
   * olusabilecek maximum baglanti sayisini ifade eder.Pool bu degere ulastiginda ve idle durumda
   * baglanti yoksa getConnection() metodu connectionTimeOut parametresi ile belirlenmis deger kadar
   * bloklanacaktir.Bu deger asilirsa timeout olacaktir.maximumPoolSize default degeri 10'dur.
   *
   * <p>poolName - Bu ozellik ile connection pool'larimiza loglama vb amaclarla isim verebiliriz.
   *
   * <p>initializationFailTimeOut - Bu ozellik havuza basarili bir sekilde connection eklenemedigi
   * durumda ne kadar surede fail-fast bir sekilde exception firlatacagini kontrol eder. Bu timeout
   * suresi connectionTimeout suresinden sonra devreye girer.
   *
   * <p>readOnly - Bu ozellik Pool'dan alinan connectionlarin default olarak read-only bir sekilde
   * olusup olusmayacagini kontrol eder.
   *
   * <p>validationTimeout - Bu ozellik bir baglantinin canliligini test edilecegi maksimum sureyi kontrol eder. Bu
   * ozellik connectionTimeout suresinden az olmalidir. En az 250 ms olabilir . Default 5000ms.
   *
   * <p>threadFactory - Bu ozellik pool icin threadler uretilirken kullanilacak ThreadFactory'i
   * belirler.Default olarak IoC container veya uygulama tarafindan yonetilir.
   *
   * <p><a href="https://github.com/brettwooldridge/HikariCP/blob/dev/README.md">HikariCP Github
   * Link</a>
   */
//    @Bean("hikariDataSource")
//    public DataSource dataSource() {
//
//      HikariConfig hikariConfig = new HikariConfig();
//      hikariConfig.setJdbcUrl(url);
//      hikariConfig.setUsername(username);
//      hikariConfig.setPassword(password);
//      hikariConfig.addDataSourceProperty("minimumIdle", "5");
//      return new HikariDataSource(hikariConfig);
//    }

  @Bean("hikariDataSourceV1")
  @ConfigurationProperties("spring.datasource.ouz.hikari")
  public DataSource dataSourceV1(
      @Qualifier("dataSourcePropertiesOuz") DataSourceProperties dataSourceProperties) {
    return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
  }

  @Bean("dataSourcePropertiesOuz")
  @ConfigurationProperties("spring.datasource.ouz")
  public DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean("jdbcTemplateWithHikariDataSource")
  public JdbcTemplate jdbcTemplateWithHikari(
      @Qualifier("hikariDataSourceV1") DataSource datasource) {
    return new JdbcTemplate(datasource);
  }

  @Bean("namedJdbcTemplateWithHikariDataSource")
  public NamedParameterJdbcTemplate namedParameterJdbcTemplateWithHikari(
      @Qualifier("hikariDataSourceV1") DataSource datasource) {
    return new NamedParameterJdbcTemplate(datasource);
  }
}
