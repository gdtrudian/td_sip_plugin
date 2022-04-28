#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint td_sip_plugin.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'td_sip_plugin'
  s.version          = '0.0.1'
  s.summary          = 'A Flutter plugin for sip belong Trudian.'
  s.description      = <<-DESC
A Flutter plugin for sip belong Trudian.
                       DESC
  s.homepage         = 'http://open.trudian.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Jeason' => '1691665955@qq.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.dependency 'TDSip','1.0.3'
  s.platform = :ios, '9.0'

  s.pod_target_xcconfig = { 'VALID_ARCHS' => 'x86_64 armv7 arm64' }
end
